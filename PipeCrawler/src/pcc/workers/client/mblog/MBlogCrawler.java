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
import jpipe.util.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pcc.core.CrawlerSetting;
import pcc.core.entity.MBlog;
import pcc.core.entity.MBlogTask;
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

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<MBlogTask> outputBuffer = (Buffer<MBlogTask>) getBufferStore().use("finishedtasks");
        Buffer<MBlogTask> taskBuffer = (Buffer<MBlogTask>) getBufferStore().use("tasks");
        Buffer<MBlogTask> failed_taskBuffer = (Buffer<MBlogTask>) getBufferStore().use("failedtasks");

        Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");

        LUBuffer<ClientConnector.IClientProtocol> messageBuffer
                = (LUBuffer<ClientConnector.IClientProtocol>) this.getBufferStore().use("msg");

        MBlogTask task = null;

        task = (MBlogTask) failed_taskBuffer.poll(this);
        if (task == null) {
            task = (MBlogTask) taskBuffer.poll(this);
        }
        if (task == null) {
            //no user in the input inituser buffer, add a request msg to msgbuffer
            MBlogTaskRequest request = new MBlogTaskRequest();
            blockedpush(messageBuffer, request);
            //now wait for input buffer to be filled with init users
            task = (MBlogTask) blockedpoll(taskBuffer);
        }

        CrawlerClient client = CrawlerConnectionManager.getNewClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());

        if (CrawlerSetting.USE_PROXY) {
            if (proxy == null) {

                proxy = (Proxy) blockedpoll(proxybuffer);
            }
            proxy = new Proxy(proxy.getHost(), proxy.getPort());

            client.setProxy(proxy);
            //System.out.println("Connecting using proxy = " + proxy);
        }

        String json = null;
        int count;
        //System.out.println("Crawling id=" + task.getUser_id());
        //m.weibo.cn/page/json?containerid=100505"+ id +"_-_WEIBO_SECOND_PROFILE_WEIBO&page="+num
        String url = null;
        try {
            int num = task.getPage_num();
            url = "http://m.weibo.cn/page/json?containerid=100505" + task.getUser_id() + "_-_WEIBO_SECOND_PROFILE_WEIBO&page=" + num;
            json = client.wget(url);

            if (json.contains("\"mod_type\":\"mod\\/empty\"")) {
                if (num == 1) {
                    //this account does not have any blog, return empty list
                    task.getSubtask().setSubTask_done(num, new ArrayList<>());
                    task.getSubtask().printStatus();
                    System.out.println("User id=" + task.getUser_id() + " with no mblog");
                    blockedpush(outputBuffer, task);
                    try {
                        Thread.sleep(4000);
                    } catch (Exception ex) {
                    }
                    return Worker.SUCCESS;
                } else {
                    throw new Exception("Empty content respond");
                }
            }

            JSONParser parser = new JSONParser();
            JSONObject doc = ((JSONObject) parser.parse(json));
            count = Integer.valueOf(doc.get("count").toString());
            JSONArray cards = (JSONArray) doc.get("cards");
            JSONArray cards_grouop = ((JSONArray) ((JSONObject) cards.get(0)).get("card_group"));
            List<MBlog> blogs = new ArrayList<>();
            for (int i = 0; i < cards_grouop.size(); i++) {
                JSONObject mblog = (JSONObject) ((JSONObject) cards_grouop.get(i)).get("mblog");
                MBlog b = fromJSON(mblog, task.getUser_id());
                blogs.add(b);
            }

            task.getSubtask().setSubTask_done(num, blogs);
            //System.out.println("Task done id=" + task.getUser_id() + ", pagenum=" + num + "");
            if (task.getPage_num() == 1 && task.getMax_page_num() == 1) {
                //if this is the first page of the user
                //check if there are more pages to crawl
                if (count > 10) {
                    int total = Math.floorDiv((count - 1), 10) + 1;
                    task.getSubtask().setMax_page_num(total);
                    for (int i = 2; i <= total; i++) {

                        MBlogTask newTask = new MBlogTask(task);
                        newTask.setPage_num(i);

                        blockedpush(taskBuffer, newTask);

                    }
                    //System.out.println("Initiated " + (total - 1) + " sub tasks");
                }

            } else {
            }
            if (task.AllDone()) {
                System.out.println("User id=" + task.getUser_id() + " Done, n=" + task.getResults().size());
                task.getSubtask().printStatus();
                task.removeDupResult();
                blockedpush(outputBuffer, task);
            }
            try {

                Thread.sleep(4000);
            } catch (Exception ex) {

            }

            return Worker.SUCCESS;
        } catch (Exception ex) {
            this.proxy = null;
            if (json != null) {
                //Logger.getLogger(MBlogCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("url=" + url);
            //System.out.println("JSON = " + json);
            blockedpush(failed_taskBuffer, task);
            return Worker.FAIL;
        }

    }

}
