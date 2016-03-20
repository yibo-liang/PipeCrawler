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
package pcc.workers.client.mblog;

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import pcc.core.entity.MBlogTask;
import pcc.workers.client.common.ClientConnector;
import pcc.workers.client.protocols.MBlogUploadRequest;

/**
 *
 * @author yl9
 */
public class BlogResultCollector extends Worker {

    int num = 40;

    @Override
    public int work() {

        Buffer<MBlogTask> inputbuffer = (Buffer<MBlogTask>) getBufferStore().use("finishedtasks");
        Buffer<ClientConnector.IClientProtocol> msgbuffer = getBufferStore().use("msg");

        if (inputbuffer.getCount() > num) {

            MBlogTask[] data = new MBlogTask[num];
            for (int i = 0; i < num; i++) {
                data[i] = (MBlogTask) blockedpoll(inputbuffer);
            }
            MBlogUploadRequest request=new MBlogUploadRequest(data);
            blockedpush(msgbuffer, request);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlogResultCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Worker.SUCCESS;

    }

}
