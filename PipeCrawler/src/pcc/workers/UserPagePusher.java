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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import jpipe.buffer.util.BufferStore;
import jpipe.interfaceclass.IBuffer;
import jpipe.util.Triplet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pcc.core.CrawlerSetting;
import pcc.http.CrawlerClient;
import pcc.http.UserAgentHelper;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class UserPagePusher extends Worker {

    private HashMap<String, Boolean> subassignments;
    private long assignNum;
    private long workDone;
    private Proxy proxy = null;

    private boolean AllDone() {
        if (workDone != assignNum) {
            return false;
        } else {
            Iterator i = subassignments.entrySet().iterator();
            for (; i.hasNext();) {
                Map.Entry pair = (Map.Entry) i.next();
                if (!(Boolean) pair.getValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    public void notifyDone(Object i) {
        synchronized (this) {
            //System.out.println("User Pusher get notified." + i + ", " + workDone + "/" + assignNum);
            subassignments.put((String) i, true);
            workDone++;
            notifyAll();
        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<String> inputbuffer = (LUBuffer) this.getBufferStore().use("containerid");
        Buffer<String> failbuffer = (LUBuffer) this.getBufferStore().use("failedcontainerid");

        Buffer<Triplet> outputbuffer = (LUBuffer) this.getBufferStore().use("pagelist");

        subassignments = new HashMap<>();
        assignNum = 0;
        workDone = 0;
        //System.out.println("USER PAGE PUSHER STarts;");
        String containerid = (String) failbuffer.poll(this);
        if (containerid == null) {
            containerid = (String) inputbuffer.poll(this);
        }
        if (containerid == null) {
            //System.out.println("NOOOOOINN[PUPT");
            return Worker.NO_INPUT;
        }

        CrawlerClient client = new CrawlerClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());
        if (CrawlerSetting.USE_PROXY) {
            if (this.proxy == null) {
               // System.out.println("poolllllllling");

                Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");
               // System.out.println(Objects.isNull(proxybuffer));
                this.proxy = (Proxy) proxybuffer.poll(this);
                while (this.proxy == null) {
                    this.proxy = (Proxy) proxybuffer.poll(this);
                }
            }
           // System.out.println("Connecting using proxy = " + this.proxy);
            client.setProxy(this.proxy);

        }

        String json1 = "";
        String json2 = "";
        JSONParser parser = new JSONParser();
        Long fanNum;
        Long followNum;
        try {
            json1 = client.wget("http://m.weibo.cn/page/json?containerid=" + containerid + "_-_FOLLOWERS&page=1");
            json2 = client.wget("http://m.weibo.cn/page/json?containerid=" + containerid + "_-_FANS&page=1");
            //System.out.println(json1);
            //System.out.println(json2);

            JSONObject obj1 = (JSONObject) parser.parse(json1);
            JSONObject obj2 = (JSONObject) parser.parse(json2);

            followNum = (Long) obj1.get("count") / 10 + 1;
            fanNum = (Long) obj2.get("count") / 10 + 1;
        } catch (Exception ex) {
            //Logger.getLogger(UserPagePusher.class.getName()).log(Level.SEVERE, null, ex);
            blockedpush(failbuffer, containerid);
            this.proxy = null;
            //System.out.println("UPP Fails");
            //System.out.println(json1);
            //System.out.println(json2);
            return Worker.FAIL;
        }
        //System.out.println("pushing");
        assignNum = followNum + fanNum;
        for (long i = 1; i <= followNum; i++) {

            blockedpush(outputbuffer, new Triplet(this, "follow" + i, "http://m.weibo.cn/page/json?containerid=" + containerid + "_-_FOLLOWERS&page=" + i));
          //  System.out.println("Giving out task follow" + i);
            subassignments.put("follow" + i, false);

        }
        for (long i = 1; i <= fanNum; i++) {
            blockedpush(outputbuffer, new Triplet(this, "fans" + i, "http://m.weibo.cn/page/json?containerid=" + containerid + "_-_FANS&page=" + i));
           // System.out.println("Giving out task fans" + i);
            subassignments.put("fans" + i, false);

        }
        //System.out.println("all pushed");
        synchronized (this) {
            while (!AllDone()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(UserPagePusher.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
            System.out.println("All done!");
            return Worker.SUCCESS;
        }

    }

}
