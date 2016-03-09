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
package pcc.workers.client.accountdetail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pcc.core.CrawlerSetting;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.RawAccount;
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.UserAgentHelper;
import pcc.http.entity.Proxy;
import pcc.workers.client.common.ClientConnector;
import pcc.workers.client.protocols.DetailTaskRequest;

/**
 * This worker takes an user id which is 10 digits long, and turns it to a
 * container id which Sina Weibo uses to acquire its information and fans list
 *
 * @author yl9
 */
public class DetailCrawler extends Worker {

    private Proxy proxy = null;

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<RawAccount> inputBuffer = this.getBufferStore().use("rawusers");
        Buffer<String> OutputBuffer = this.getBufferStore().use("account_detail");
        Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");
        LUBuffer<ClientConnector.IClientProtocol> messageBuffer
                = (LUBuffer<ClientConnector.IClientProtocol>) this.getBufferStore().use("msg");

        RawAccount temp = null;
        temp = (RawAccount) inputBuffer.poll(this);
        if (temp == null) {
            //no user in the input inituser buffer, add a request msg to msgbuffer
            DetailTaskRequest request = new DetailTaskRequest();
            blockedpush(messageBuffer, request);
            //now wait for input buffer to be filled with init users
            temp = (RawAccount) blockedpoll(inputBuffer);
        }

        if (temp == null) {
            return NO_INPUT;
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
            //proxy = new Proxy(proxy.getHost(), proxy.getPort());

            client.setProxy(proxy);
            //System.out.println("Connecting using proxy = " + proxy);
        }
        String result = "";
        try {
            result = client.wget("http://m.weibo.cn/u/" + temp.getUid());
            client.close();
            if (result != null) {
                //System.out.println(this.getPID()+", "+result);
                String identifier = "window.$render_data = ";
                int i = result.indexOf(identifier);
                int j = result.indexOf("\"mod_type\":\"mod\\/empty\"");
                if (i != -1 && j == -1) {
                    int start = i + identifier.length();
                    String tmpstr[] = result.substring(start).split(";");
                    String usr_json = tmpstr[0];
                    usr_json = usr_json.replaceAll("'", "\"");
                    //System.out.println(usr_json);

                    //handle json
                    JSONParser parser = new JSONParser();
                    JSONObject usrObj = ((JSONObject) parser.parse(usr_json));
                    JSONObject stageObj = (JSONObject) usrObj.get("stage");
                    JSONArray pageArr = (JSONArray) stageObj.get("page");
                    JSONObject infoObj = (JSONObject) pageArr.get(1);

                    AccountDetail detail = new AccountDetail();

                    detail.setUid(temp.getUid());
                    detail.setContainerid(new Long("100505" + temp.getUid()));
                    detail.setAtt_num(Integer.parseInt((String) infoObj.get("attNum")));
                    detail.setAvatar_img((String) infoObj.get("avatar_hd"));

                    detail.setBackground((String) infoObj.get("background"));
                    detail.setBlog_num(Integer.parseInt((String) infoObj.get("mblogNum")));
                    detail.setDescription((String) infoObj.get("description"));
                    detail.setFans_num(Integer.parseInt((String) infoObj.get("fansNum")));

                    String ta = (String) infoObj.get("ta");
                    detail.setGender(ta.equals("\\u5979") ? 0
                            : (ta.equals("\\u4ED6") ? 1
                                    : 2));
                    detail.setMember_rank(Integer.parseInt((String) infoObj.get("mbrank")));
                    detail.setMember_type(Integer.parseInt((String) infoObj.get("mbtype")));
                    detail.setName((String) infoObj.get("name"));
                    detail.setNative_place((String) infoObj.get("nativePlace"));
                    long unixTime = System.currentTimeMillis() / 1000L;
                    detail.setUpdate_time((int) unixTime);
                    detail.setV_type(Integer.parseInt((String) infoObj.get("verified_type")));

                    String verified = (String) infoObj.get("verified");
                    detail.setVerified((verified.equals("1")));

                    try {
                        //"Sat Sep 25 18:45:20 +0800 2010"
                        DateFormat dfm = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
                        long unixtime = dfm.parse((String) infoObj.get("created_at")).getTime() / 1000;
                        detail.setCreate_time((int) unixtime);
                    } catch (ParseException ex) {
                        detail.setCreate_time(0);
                    }

                    blockedpush(OutputBuffer, detail);
                    Thread.sleep(3000);
                    return Worker.SUCCESS;
                } else {
                    throw new Exception("Retrieved Null");
                }
            } else {
                throw new Exception("Retrieved Null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.proxy = null;
            System.out.println("result=\n" + result);
            System.out.println("RETRIEVED NULL.....");
            //this.blockedpush(inputBuffer, temp);
            return Worker.FAIL;
        }

    }

}
