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
package pcc.workers.server;

import jpipe.abstractclass.worker.Worker;
import jpipe.abstractclass.buffer.Buffer;
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class ProxySupplier extends Worker {

    private int num = 80;

    public ProxySupplier() {

    }

    public ProxySupplier(int num) {
        this.num = num;
    }

    @Override
    public int work() {

        Buffer<Proxy> outputBuffer = this.getBufferStore().use("rawproxies");

        CrawlerClient client = CrawlerConnectionManager.getNewClient();

        try {
            String temp = client.wget("http://www.tkdaili.com/api/getiplist.aspx?vkey=22207159506CEC21A5DD188A458AE121&num=" + num + "&high=1&style=3");
            //String temp = client.wget("http://qsdrk.daili666api.com/ip/?tid=559179489916758&num="+num+"&delay=3&category=2&filter=on");
            client.close();
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
            //System.out.println("Pushed "+num+" proxies.");
            Thread.sleep(5000);
            return Worker.SUCCESS;

        } catch (Exception ex) {
            // Logger.getLogger(ProxySupplier.class.getName()).log(Level.SEVERE, null, ex);
            //ex.printStackTrace();
            client.close();
            return Worker.FAIL;
        }
    }

}
