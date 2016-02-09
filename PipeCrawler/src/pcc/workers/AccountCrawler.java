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
package pcc.workers;

import java.util.Iterator;
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
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.UserAgentHelper;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class AccountCrawler extends Worker {

    private Proxy proxy = null;

    int failCount = 0;

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<Triplet> inputBuffer = (Buffer<Triplet>) getBufferStore().use("pagelist");
        Buffer<Triplet> failbuffer = (Buffer) getBufferStore().use("failedpagelist");
        Buffer<Proxy> recycleBuffer = (Buffer<Proxy>) this.getBufferStore().use("recycledproxy");

        Buffer<String> outputBuffer = (Buffer<String>) getBufferStore().use("users");
        Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");

        Triplet<UserPagePusher, String, String> temp;
        // System.out.println("Account Crawler: Trying to get from Failed page list first");
        temp = (Triplet<UserPagePusher, String, String>) failbuffer.poll(this);
        if (temp == null) {

            // System.out.println("Account Crawler: Trying to get from page list");
            temp = (Triplet<UserPagePusher, String, String>)blockedpoll (inputBuffer);
            if (temp == null) {
                setState(WorkerStates.POST_FAIL);
                System.out.println("CRAWLER NO INPUT, id="+this.getPID());
                return Worker.NO_INPUT;
            }
            //System.out.println("polling");
        }

        //TPBuffer<Object> outputBuffer = (QBufferLocked<Object>) buffers[1];
        //TPBuffer<String> failBuffer = (QBufferLocked<String>) buffers[2];
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
        String json=null;
        try {

            //System.out.println(temp.getThird());
            this.setState(WorkerStates.WORKING);
             json = client.wget(temp.getThird());
          
            if (json.contains("{\"mod_type\":\"mod\\/empty\",\"msg\":\"\\u6ca1\\u6709\\u5185\\u5bb9\"}")) {
                temp.getFirst().notifyDone(temp.getSecond());
                this.setState(WorkerStates.POST_SUCCESS);
                Thread.sleep(2000);
                failCount = 0;
                return Worker.SUCCESS;
            }
            JSONParser parser = new JSONParser();

            JSONObject obj1 = ((JSONObject) parser.parse(json));
            JSONArray a1 = (JSONArray) obj1.get("cards");
            JSONObject obj2 = (JSONObject) a1.get(0);
            JSONArray a2 = (JSONArray) obj2.get("card_group");

            for (Iterator i = a2.iterator(); i.hasNext();) {
                JSONObject userObj = (JSONObject) ((JSONObject) i.next()).get("user");
                //  System.out.println(userObj.toString());
                //while (!outputBuffer.push(userObj.toString()));

                blockedpush(outputBuffer, userObj.get("id").toString());

            }
            temp.getFirst().notifyDone(temp.getSecond());
            this.setState(WorkerStates.POST_SUCCESS);
            failCount = 0;
            client.close();
            Thread.sleep(4000);
        } catch (InterruptedException | ParseException |NullPointerException ex) {
            //Logger.getLogger(AccountCrawler.class.getName()).log(Level.SEVERE, null, ex);
            blockedpush(failbuffer, temp);
            if(ex instanceof ParseException){
                //System.out.println("Fail on P="+proxy);
               // System.out.println(json);
            }
            //temp.getFirst().notifyDone(temp.getSecond());
            //  System.out.println("Fail work with " + this.proxy);
            failCount++;
            if (failCount % 5 == 0) {
                System.out.println("Failed " + failCount + " times in a row." + this.getClass().getName() + " proxy:" + this.proxy + ",\n task=" + temp.getThird());
            }

            //recycleBuffer.push(this, proxy);
            proxy = null;
            client.close();
            setState(WorkerStates.POST_FAIL);
            try {

                Thread.sleep(1000);
            } catch (InterruptedException ex1) {
                //Logger.getLogger(AccountCrawler.class.getName()).log(Level.SEVERE, null, ex1);
            }
            
            return Worker.FAIL;
        }

        return Worker.SUCCESS;
    }

}
