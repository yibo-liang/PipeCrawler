/*
 * The MIT License
 *
 * Copyright 2016 yl9.
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
package pcc.workers.client.protocols;

import jpipe.abstractclass.buffer.Buffer;
import pcc.core.entity.MessageCarrier;
import pcc.core.entity.RawAccount;
import pcc.workers.client.common.ClientConnector;

/**
 *
 * @author yl9
 */
public class DetailTaskRequest implements ClientConnector.IClientProtocol {

    ClientConnector connector;

    @Override
    public MessageCarrier messageToServer(ClientConnector connector) {
        this.connector = connector;
        MessageCarrier r = new MessageCarrier("DetailTask", new Integer(5));
        return r;
    }

    @Override
    public void messageFromServer(MessageCarrier msg) {
        if (!msg.getMsg().equals("NULL")) {
            Buffer<RawAccount> user_buffer = this.connector.getBufferStore().use("rawusers");

            try {
                RawAccount[] rusers = (RawAccount[]) msg.getObj();
                for (int i = 0; i < rusers.length; i++) {
                    this.connector.blockedpush(user_buffer, rusers[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            DetailTaskRequest request=new DetailTaskRequest();
            Buffer<ClientConnector.IClientProtocol> b= this.connector.getBufferStore().use("msg");
            b.push(connector, request);
        }
    }

}
