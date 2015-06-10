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
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.TPBuffer;
import jpipe.abstractclass.DefaultWorker;
import jpipe.buffer.util.TPBufferStore;
import jpipe.interfaceclass.IBUffer;
import jpipe.util.Triplet;
import org.json.simple.JSONArray;
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
public class AccountCrawler extends DefaultWorker {

    private Proxy proxy;

    @Override
    @SuppressWarnings("empty-statement")
    public boolean work(IBUffer[] buffers) {
        TPBuffer<Triplet> inputBuffer = (TPBuffer<Triplet>) TPBufferStore.use("pagelist");
        TPBuffer<Triplet> failbuffer = (TPBuffer) TPBufferStore.use("failedpagelist");

        TPBuffer<String> outputBuffer = (TPBuffer<String>) TPBufferStore.use("users");
        TPBuffer<Proxy> proxybuffer = (TPBuffer<Proxy>) TPBufferStore.use("proxys");

        Triplet<UserPagePusher, String, String> temp;
        temp = (Triplet<UserPagePusher, String, String>) failbuffer.poll();
        if (temp == null) {
            temp = (Triplet<UserPagePusher, String, String>) inputBuffer.poll();
            if (temp == null) {
                return false;
            }
            //System.out.println("polling");
        }

        //TPBuffer<Object> outputBuffer = (QBufferLocked<Object>) buffers[1];
        //TPBuffer<String> failBuffer = (QBufferLocked<String>) buffers[2];
        CrawlerClient client = new CrawlerClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());

        if (CrawlerSetting.USE_PROXY) {
            if (proxy == null) {
                proxy = proxybuffer.poll();
                while (proxy == null) {
                    proxy = proxybuffer.poll();
                }
            }
            client.setProxy(proxy);
            System.out.println("Connecting using proxy = " + proxy);
        }

        try {
            System.out.println(temp.getThird());
            String json = client.wget(temp.getThird());
            JSONParser parser = new JSONParser();

            JSONObject obj1 = ((JSONObject) parser.parse(json));
            JSONArray a1 = (JSONArray) obj1.get("cards");
            JSONObject obj2 = (JSONObject) a1.get(0);
            JSONArray a2 = (JSONArray) obj2.get("card_group");

            for (Iterator i = a2.iterator(); i.hasNext();) {
                JSONObject userObj = (JSONObject) ((JSONObject) i.next()).get("user");
                System.out.println(userObj.toString());
                while (!outputBuffer.push(userObj.toString()));
                temp.getFirst().notifyDone(temp.getSecond());
            }
            Thread.sleep(2000);

        } catch (Exception ex) {
            Logger.getLogger(AccountCrawler.class.getName()).log(Level.SEVERE, null, ex);
            blockedpush(failbuffer, temp);
            //temp.getFirst().notifyDone(temp.getSecond());
            return false;
        }

        return true;
    }

}
