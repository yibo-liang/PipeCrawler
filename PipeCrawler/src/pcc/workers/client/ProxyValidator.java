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
package pcc.workers.client;

//import java.util.logging.Level;
//import java.util.logging.Logger;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class ProxyValidator extends Worker {

    @Override
    public int work() {
        CrawlerClient client = CrawlerConnectionManager.getNewClient();

        try {
            LUBuffer<Proxy> inputBuffer = (LUBuffer<Proxy>) this.getBufferStore().use("rawproxys");
            LUBuffer<Proxy> recycleBuffer = (LUBuffer<Proxy>) this.getBufferStore().use("recycledproxy");

            LUBuffer<Proxy> outputBuffer = (LUBuffer<Proxy>) this.getBufferStore().use("proxys");

            Proxy p;
            p = (Proxy) recycleBuffer.poll(this);
            if (p == null) {
                p = (Proxy) blockedpoll(inputBuffer);
            } else {
                if (p.getRecycle() < 1) {
                    p.setRecycle(p.getRecycle() + 1);
                } else {
                    return Worker.FAIL;
                }
            }

            client.setProxy(p);

            String result = client.wget("http://www.lagado.com/proxy-test");
            client.close();

            if (result != null && 
                    (result.contains("This request appears NOT to have come via a proxy.")
                    || false && result.contains("This request appears to have come via a proxy"))) {
                blockedpush(outputBuffer, p);
                System.out.println("pid="+this.getPID()+", Validated IP="+p.getHost()+","+p.getPort());
                return Worker.SUCCESS;
            } else {
                //System.out.println(p.toString());
                System.out.println("pid="+this.getPID()+", Failed IP="+p.getHost()+","+p.getPort());
                //System.out.println(result);
                return Worker.FAIL;
            }
        } catch (Exception ex) {
            //Logger.getLogger(ProxyValidator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed "+ex.getMessage());
            return Worker.FAIL;
        }

    }

}