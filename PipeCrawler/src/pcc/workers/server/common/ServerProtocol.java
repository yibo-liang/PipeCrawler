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

import java.util.ArrayList;
import java.util.List;
import jpipe.abstractclass.buffer.Buffer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pcc.core.entity.MessageCarrier;
import pcc.core.entity.RawAccount;
import pcc.core.entity.AccountDetail;
import pcc.core.hibernate.DatabaseManager;
import pcc.http.entity.Proxy;
import pcc.workers.server.ServerConnector;

/**
 *
 * @author yl9
 */
public class ServerProtocol implements ServerConnector.IServerProtocol {

    private ServerConnector connector;

    public ServerProtocol(ServerConnector connector) {
        this.connector = connector;
    }

    private MessageCarrier handleUserTaskRequest(MessageCarrier mc) {
        int num = (Integer) mc.getObj();

        List<RawAccount> result = new ArrayList<>();
        Session session = DatabaseManager.getSession();
        Transaction tx = session.beginTransaction();
        long count = (long) session.
                createCriteria(RawAccount.class).
                setProjection(Projections.rowCount()).
                uniqueResult();
        long range = (count > 1000) ? 1000 : count - 1;
        boolean error = false;
        try {
            for (int i = 0; i < num; i++) {
                List<RawAccount> items;
                RawAccount item;
                do {
                    item = (RawAccount) session
                            .createCriteria(RawAccount.class)
                            .add(Restrictions.idEq(new Long((long) (count - Math.random() * range))))
                            .uniqueResult();
                } while (item == null || item.getCrawlstate() == 1);

                item.setCrawlstate(1);
                result.add(item);
                session.save(item);
                session.flush();
                session.clear();

            }
        } catch (Exception ex) {
            error = true;
        }

        tx.commit();
        session.close();
        if (result.size() > 0 && !error) {
            RawAccount[] raw_accounts = result.toArray(new RawAccount[result.size()]);
            return new MessageCarrier("RAWUSER", raw_accounts);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    private MessageCarrier handleRawUser(MessageCarrier mc) {
        RawAccount[] rusers = (RawAccount[]) mc.getObj();

        DatabaseManager.DBInterface dbi = new DatabaseManager.DBInterface();
        dbi.batchInsert(rusers);

        return new MessageCarrier("ACK", "");
    }

    private MessageCarrier handleUser(MessageCarrier mc) {
        AccountDetail[] users = (AccountDetail[]) mc.getObj();

        return null;
    }

    private MessageCarrier handleMblog(MessageCarrier mc) {

        return null;
    }

    private MessageCarrier handleRawProxy(MessageCarrier mc) {
        Buffer<Proxy> proxy_buffer = connector.getBufferStore().use("rawproxies");
        int num = (Integer) mc.getObj();
        //Proxy[] ps = new Proxy[num];
        List<Proxy> ps = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            //ps[i] = (Proxy) this.connector.blockedpoll(proxy_buffer);
            Proxy p = (Proxy) proxy_buffer.poll(connector);
            if (p != null) {
                ps.add(p);
            } else {
                break;
            }
        }
        if (ps.size() > 0) {
            Proxy[] result = ps.toArray(new Proxy[ps.size()]);

            return new MessageCarrier("rawproxies", result);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    @Override
    public MessageCarrier handleMsg(MessageCarrier mc) {
        String msg = mc.getMsg();
        MessageCarrier reply;
        switch (msg) {
            case "UserTask":
                reply = handleUserTaskRequest(mc);
                break;
            case "RawUser":
                reply = handleRawUser(mc);
                break;
            case "User":
                reply = handleUser(mc);
                break;
            case "RawProxy":
                reply = handleRawProxy(mc);
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
