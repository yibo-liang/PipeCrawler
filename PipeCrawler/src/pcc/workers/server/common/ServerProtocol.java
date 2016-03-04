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
package pcc.workers.server.common;

import pcc.core.entity.MessageCarrier;
import pcc.core.entity.RawUser;
import pcc.core.entity.User;
import pcc.workers.server.ServerConnector;

/**
 *
 * @author yl9
 */
public class ServerProtocol implements ServerConnector.IServerProtocol {

    private MessageCarrier handleRawUser(MessageCarrier mc) {
        RawUser[] rusers = (RawUser[]) mc.getObj();

        return null;
    }

    private MessageCarrier handleUser(MessageCarrier mc) {
        User[] users = (User[]) mc.getObj();

        return null;
    }

    private MessageCarrier handleMblog(MessageCarrier mc) {

        return null;
    }

    @Override
    public MessageCarrier handleMsg(MessageCarrier mc) {
        String msg = mc.getMsg();
        MessageCarrier reply;
        switch (msg) {
            case "RawUser":
                reply = handleRawUser(mc);
                break;
            case "User":
                reply = handleUser(mc);
                break;
            case "MBlog":
                reply = handleMblog(mc);
                break;
            default:
                reply = new MessageCarrier();
        }

        return reply;
    }

}
