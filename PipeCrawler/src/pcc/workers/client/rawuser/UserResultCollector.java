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
package pcc.workers.client.rawuser;

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import pcc.core.entity.RawAccount;
import pcc.workers.client.common.ClientConnector;
import pcc.workers.client.protocols.RawUserUploadRequest;

/**
 *
 * @author yl9
 */
public class UserResultCollector extends Worker {

    int num = 100;

    @Override
    public int work() {

        Buffer<RawAccount> inputbuffer = (Buffer<RawAccount>) getBufferStore().use("rawusers");
        Buffer<ClientConnector.IClientProtocol> msgbuffer = getBufferStore().use("msg");

        if (inputbuffer.getCount() > num) {

            RawAccount[] data = new RawAccount[num];
            for (int i = 0; i < num; i++) {
                data[i] = (RawAccount) blockedpoll(inputbuffer);
            }
            RawUserUploadRequest request=new RawUserUploadRequest(data);
            blockedpush(msgbuffer, request);
        } else {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserResultCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Worker.SUCCESS;

    }

}
