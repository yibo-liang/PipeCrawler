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

import jpipe.util.Pair;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.MBlog;
import pcc.core.entity.MBlogTask;
import pcc.core.entity.MessageCarrier;
import pcc.workers.client.common.ClientConnector;

/**
 *
 * @author yl9
 */
public class MBlogUploadRequest implements ClientConnector.IClientProtocol {

    private static final long serialVersionUID = 7513452012352313647L;

    MBlogTask[] data;

    public MBlogUploadRequest(MBlogTask[] data) {
        this.data = data;
    }

    @Override
    public MessageCarrier messageToServer(ClientConnector connector) {
        return new MessageCarrier("MBlog", data);
    }

    @Override
    public void messageFromServer(MessageCarrier msg) {
        System.out.println("Successfully uploaded " + data.length + " data.");
    }

}
