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
import jpipe.abstractclass.worker.Worker;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.buffer.util.BufferStore;
import jpipe.interfaceclass.IBuffer;
import pcc.http.CrawlerClient;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class ProxySupplier extends Worker {

    private int num = 5;

    public ProxySupplier() {

    }

    public ProxySupplier(int num) {
        this.num = num;
    }

    @Override
    public int work() {

        Buffer<Proxy> outputBuffer = this.getBufferStore().use("proxys");
        CrawlerClient client = new CrawlerClient();
        try {
            String temp = client.wget("http://www.tkdaili.com/api/getiplist.aspx?vkey=408521EA3728E9DFBA7C881386734BE7&num=" + num + "&high=1&style=3");

            String[] proxyarray = temp.split("\\r?\\n");
            for (String proxyarray1 : proxyarray) {
                //System.out.println("Obtained Proxy = " + proxyarray1);
                if (proxyarray1.length() > 8) {
                    String[] temp2 = proxyarray1.split(":");
                    Proxy p = new Proxy(temp2[0], temp2[1]);
                    //System.out.println("p=" + p);
                    blockedpush(outputBuffer, p);
                }
            }
            Thread.sleep(5000);
            return Worker.SUCCESS;

        } catch (Exception ex) {
           // Logger.getLogger(ProxySupplier.class.getName()).log(Level.SEVERE, null, ex);
            //ex.printStackTrace();
            return Worker.FAIL;
        }

    }

}
