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

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.TPBuffer;
import jpipe.abstractclass.Worker;
import jpipe.bufferclass.QBufferLocked;
import jpipe.interfaceclass.IBUffer;
import pcc.http.CrawlerClient;
import pcc.http.UserAgentHelper;

/**
 * This worker takes an user id which is 10 digits long, and turns it to a
 * container id which Sina Weibo uses to acquire its information and fans list
 *
 * @author yl9
 */
public class Initialiser extends Worker {

    @Override
    @SuppressWarnings("empty-statement")
    public boolean work(IBUffer[] buffers) {
        TPBuffer<String> inputBuffer = (QBufferLocked<String>) buffers[0];
        TPBuffer<String> OutputBuffer = (QBufferLocked<String>) buffers[1];

        //TPBuffer<Object> outputBuffer = (QBufferLocked<Object>) buffers[1];
        //TPBuffer<String> failBuffer = (QBufferLocked<String>) buffers[2];
        CrawlerClient client = new CrawlerClient();

        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());

        String temp = null;
        while (temp == null) {
            temp = inputBuffer.poll();
        }
        try {
            String result = client.wget("http://m.weibo.cn/u/" + temp);
            String identifier = "window.$config={'stage':'page','stageId':'";
            int i = result.indexOf(identifier);

            String containerid = result.substring(i + identifier.length(), i + identifier.length() + 16);
            System.out.println("Done! " + temp + " -> " + containerid);
            OutputBuffer.push(containerid);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Initialiser.class.getName()).log(Level.SEVERE, null, ex);

            while (!inputBuffer.push(temp)) ;
            return false;
        }

    }

}
