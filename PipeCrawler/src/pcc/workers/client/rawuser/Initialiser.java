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
package pcc.workers.client.rawuser;

import pcc.workers.client.protocols.RawUserTaskRequest;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import pcc.core.CrawlerSetting;
import pcc.core.entity.RawAccount;
import pcc.http.CrawlerClient;
import pcc.http.CrawlerConnectionManager;
import pcc.http.UserAgentHelper;
import pcc.http.entity.Proxy;
import pcc.workers.client.common.ClientConnector;

/**
 * This worker takes an user id which is 10 digits long, and turns it to a
 * container id which Sina Weibo uses to acquire its information and fans list
 *
 * @author yl9
 */
public class Initialiser extends Worker {

    private Proxy proxy = null;

    @Override
    @SuppressWarnings("empty-statement")
    public int work() {
        Buffer<RawAccount> inputBuffer = this.getBufferStore().use("initusers");
        Buffer<String> OutputBuffer = this.getBufferStore().use("containerid");
        Buffer<Proxy> proxybuffer = (Buffer<Proxy>) getBufferStore().use("proxys");
        LUBuffer<ClientConnector.IClientProtocol> messageBuffer
                = (LUBuffer<ClientConnector.IClientProtocol>) this.getBufferStore().use("msg");
        //TPBuffer<Object> outputBuffer = (LUBuffer<Object>) buffers[1];
        //TPBuffer<String> failBuffer = (LUBuffer<String>) buffers[2];
        RawAccount temp = null;
        temp = (RawAccount) inputBuffer.poll(this);
        if (temp == null) {
            //no user in the input inituser buffer, add a request msg to msgbuffer
            RawUserTaskRequest request = new RawUserTaskRequest();
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
                String identifier = "window.$config={'stage':'page','stageId':'";
                int i = result.indexOf(identifier);
                if (i != -1) {
                    String containerid = result.substring(i + identifier.length(), i + identifier.length() + 16);
                    //System.out.println("Done! " + temp + " -> " + containerid);
                    if (!containerid.matches("[0-9]+")) {
                        throw new Exception("Retrieved Null");
                    }
                    this.blockedpush(OutputBuffer, containerid);
                    //System.out.println("Pushed");
                    Thread.sleep(4000);
                    return Worker.SUCCESS;
                } else {
                    throw new Exception("Retrieved Null");
                }
            } else {
                throw new Exception("Retrieved Null");
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            this.proxy = null;
            System.out.println("result=\n" + result);
            System.out.println("RETRIEVED NULL.....");
            this.blockedpush(inputBuffer, temp);
            return Worker.FAIL;
        }

    }

}
