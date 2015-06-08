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
import jpipe.util.Triplet;
import pcc.http.CrawlerClient;
import pcc.http.UserAgentHelper;

/**
 *
 * @author yl9
 */
public class AccountCrawler extends Worker {

    @Override
    @SuppressWarnings("empty-statement")
    public boolean work(IBUffer[] buffers) {
        TPBuffer<Triplet> inputBuffer = (QBufferLocked<String>) buffers[0];
        TPBuffer<String> outputBuffer = (QBufferLocked<String>) buffers[1];
        //TPBuffer<Object> outputBuffer = (QBufferLocked<Object>) buffers[1];
        //TPBuffer<String> failBuffer = (QBufferLocked<String>) buffers[2];

        CrawlerClient client = new CrawlerClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.addHeader(UserAgentHelper.iphone6plusAgent());

        Triplet<UserPagePusher, String, String> temp = null;
        while (temp == null) {

            temp = (Triplet<UserPagePusher, String, String>) inputBuffer.poll();
            //System.out.println("polling");
        }
        try {
            System.out.println(temp.getThird());
            String json = client.wget(temp.getThird());
            //System.out.println(result);
            while (!outputBuffer.push(json));
            temp.getFirst().notifyDone(temp.getSecond());
            
        } catch (Exception ex) {
            Logger.getLogger(AccountCrawler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

}
