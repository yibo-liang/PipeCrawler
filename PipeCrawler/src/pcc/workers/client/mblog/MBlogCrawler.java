/*
 * The MIT License
 *
 * Copyright 2015 yl9.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pcc.workers.client.mblog;

import java.util.ArrayList;
import pcc.workers.client.rawuser.*;
import pcc.workers.client.rawuser.UserPagePusher;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import jpip.singletons.WorkerStates;
import jpipe.util.Pair;
import jpipe.util.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pcc.core.CrawlerSetting;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.MBlog;
import pcc.core.entity.MBlogTask;
import pcc.core.entity.MBlogTaskResult;
import pcc.core.entity.MBlogTaskResult.PostInfo;
import pcc.core.entity.RawAccount;
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.UserAgentHelper;
import pcc.http.entity.Proxy;
import pcc.workers.client.common.ClientConnector;
import pcc.workers.client.protocols.MBlogTaskRequest;

/**
 *
 * @author yl9
 */
public class MBlogCrawler extends Worker {

    private Proxy proxy = null;

    int failCount = 0;

    private int currentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    private MBlog fromJSON(JSONObject jobj, long user_id) {
        MBlog result = new MBlog();
        /*
         this.post_id=doc.getLong("post_id");
         this.user_id=doc.getLong("user_id");
         this.create_timestamp=doc.getInt("create_timestamp");
         this.update_timestamp=doc.getInt("update_timestamp");
         this.repost_count=doc.getInt("repost_count");
         this.comments_count=doc.getInt("comments_count");
         this.attitudes_count=doc.getInt("attitudes_count");
         this.like_count=doc.getInt("like_count");
         this.picture_count=doc.getInt("picture_count");
         this.is_video=doc.getBoolean("is_video");
         this.is_retweet=doc.getBoolean("is_retweet");
         this.retweet_post_id=doc.getLong("retweet_post_id");
         this.mblogtype=doc.getInt("mblogtype");
         this.is_long_text=doc.getBoolean("is_long_text");
         this.page_title=doc.getString("page_title");
         */
        result.setUser_id(user_id);
        result.setPost_id(Long.valueOf(jobj.get("id").toString()));
        result.setCreate_timestamp(Integer.valueOf(jobj.get("created_timestamp").toString()));
        result.setUpdate_timestamp(currentTimestamp());
        result.setRepost_count(Integer.valueOf(jobj.get("reposts_count").toString()));
        result.setComments_count(Integer.valueOf(jobj.get("comments_count").toString()));
        result.setAttitudes_count(Integer.valueOf(jobj.get("attitudes_count").toString()));
        result.setLike_count(Integer.valueOf(jobj.get("like_count").toString()));
        result.setPicture_count(((JSONArray) jobj.get("pic_ids")).size());
        result.setIs_video(false);

        Object page_info = jobj.get("page_info");
        if (page_info != null) {
            Object object_type = ((JSONObject) page_info).get("object_type");
            if (object_type != null && (object_type.toString()).equals("video")) {
                result.setIs_video(true);
            }

            result.setPage_title(((JSONObject) page_info).get("page_title").toString());
        }

        result.setIs_retweet(false);
        Object retweeted_status = jobj.get("retweeted_status");
        if (retweeted_status != null) {
            result.setIs_retweet(true);
            long id = Long.valueOf(((JSONObject) retweeted_status).get("id").toString());
            result.setRetweet_post_id(id);
        }
        result.setMblogtype(Integer.valueOf(jobj.get("mblogtype").toString()));
        result.setIs_long_text(Boolean.valueOf(jobj.get("isLongText").toString()));
        result.setText(jobj.get("text").toString());
        return result;

    }

    private void switchProxy() {
        Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");
        if (CrawlerSetting.USE_PROXY) {
            if (proxy == null) {

                proxy = (Proxy) blockedpoll(proxybuffer);
            }
            //proxy = new Proxy(proxy.getHost(), proxy.getPort());

            //System.out.println("Connecting using proxy = " + proxy);
        }
    }

    private Pair<Integer, ArrayList<PostInfo>> analysePage(AccountDetail acc, String userid, int page) throws Exception {
        ArrayList<PostInfo> results = new ArrayList<>();

        String url = "http://m.weibo.cn/page/json?containerid=100505" + userid + "_-_WEIBO_SECOND_PROFILE_WEIBO&page=" + page;

        CrawlerClient client = CrawlerConnectionManager.getNewClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());
        client.setProxy(proxy);
        String json = client.wget(url);

        client.close();
        if (json == null) {
            throw new Exception("wget null");
        }
        if (json.contains("\"mod_type\":\"mod\\/empty\"")) {
            if (acc.getBlog_num() == 0) {

                return new Pair<>(new Integer(0), results);
            } else {

                System.out.println("************* found empty id=" + userid);
                //System.out.println(json);
                throw new Exception("Proxy expired or not valid");
            }
        }

        JSONParser parser = new JSONParser();
        JSONObject doc;
        try {
            doc = ((JSONObject) parser.parse(json));

        } catch (Exception ex) {
            System.out.println(" - -  - - - -Parse ERROR - - - - ");
            //System.out.println(json);
            throw ex;
        }

        int count = Integer.parseInt(doc.get("count").toString());
        if (count == 0) {
            System.out.println("--------------ERROR");
            //System.out.println(doc.toJSONString());
        }
        JSONArray cards = (JSONArray) doc.get("cards");
        JSONArray cards_grouop = ((JSONArray) ((JSONObject) cards.get(0)).get("card_group"));
        for (int i = 0; i < cards_grouop.size(); i++) {
            JSONObject mblog = (JSONObject) ((JSONObject) cards_grouop.get(i)).get("mblog");
            MBlog b = fromJSON(mblog, Long.parseLong(userid));
            MBlogTaskResult.PostInfo info = new MBlogTaskResult.PostInfo();
            info.setPostid(b.getPost_id());
            info.setTimestamp(b.getCreate_timestamp());
            info.setAttitudes_count(b.getAttitudes_count());
            info.setComments_count(b.getComments_count());
            info.setIs_retweet(b.isIs_retweet());
            info.setLike_count(b.getLike_count());
            info.setRepost_count(b.getRepost_count());

            results.add(info);
            //System.out.println("Got for id=" + userid + " time= " + info.getTimestamp());
        }
        return new Pair<>(new Integer(count), results);

    }

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<MBlogTaskResult> outputBuffer = (Buffer<MBlogTaskResult>) getBufferStore().use("finishedtasks");
        LUBuffer<AccountDetail> taskBuffer = (LUBuffer<AccountDetail>) getBufferStore().use("tasks");

        AccountDetail acc;

        acc = (AccountDetail) taskBuffer.poll(this);
        if (acc == null) {
            LUBuffer<ClientConnector.IClientProtocol> messageBuffer
                    = (LUBuffer<ClientConnector.IClientProtocol>) this.getBufferStore().use("msg");

            MBlogTaskRequest request = new MBlogTaskRequest();
            blockedpush(messageBuffer, request);
            acc = (AccountDetail) blockedpoll(taskBuffer);
        }

        MBlogTaskResult result = new MBlogTaskResult();
        result.setAccount(acc);

        long id = acc.getUid();
        boolean done = false;
        switchProxy();
        int k = 1;
        int max = 1;
        do {
            try {
                Pair<Integer, ArrayList<PostInfo>> p1 = analysePage(acc, String.valueOf(id), k);
                if (p1.getSecond().size() == 0 && acc.getBlog_num() > 0) {
                    System.out.println("********* count= " + p1.getFirst());
                    System.out.println("******** ERROR");
                    System.out.println(acc.toBSONDocument().toJson());
                    //break;
                }
                result.getPostinfo().addAll(p1.getSecond());

                if (k == 1) {
                    int count = p1.getFirst();
                    if (count > 10) {
                        max = (count - 1) / 10 + 1;
                    }
                    // max 35 pages, 350 posts
                    if (max > 35) {
                        max = 35;
                    }
                }
                k++;
                if (k > max) {
                    done = true;
                }
                try {
                    this.setState(WorkerStates.POST_SUCCESS);
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                proxy = null;
                switchProxy();
            }
        } while (!done);

        if (result.getPostinfo().size() == 0 && acc.getBlog_num() > 0) {

        }
        result.removeDup();
        blockedpush(outputBuffer, result);
        System.out.println("Get mblog info count=" + result.getPostinfo().size() + " for ID= " + result.getAccount().getUid());
        return Worker.SUCCESS;
    }

}
