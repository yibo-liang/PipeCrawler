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
import org.hibernate.criterion.Order;
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
        Query q = session.createSQLQuery("SELECT `AUTO_INCREMENT` "
                + "FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'ylproj' "
                + "AND TABLE_NAME = 'raw_account';");

        long count = new Long(q.list().get(0).toString());
        long range = (count > 100) ? 100 : count - 1;

        double pick = Math.random();
        long lower = (long) (pick * count);
        long upper = lower + range;

        boolean error = false;
        try {
            List<RawAccount> items;
            items = session.createCriteria(RawAccount.class)
                    .add(Restrictions.gt("id", new Long(lower)))
                    .add(Restrictions.le("id", new Long(upper)))
                    .add(Restrictions.eq("crawlstate", 0))
                    .addOrder(Order.asc("uid"))
                    .setMaxResults(num)
                    .list();

            RawAccount item;
            /*
             for (int i = 0; i < num; i++) {

                
             do {
             item = (RawAccount) session
             .createCriteria(RawAccount.class)
             .add(Restrictions.eq("id", new Long((long) (count - Math.random() * range))))
             .uniqueResult();
             } while (item == null || item.getCrawlstate() == 1);
             */
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);
                item.setCrawlstate(1);
                result.add(item);
                System.out.println("Give : " + item.getUid());
                session.save(item);
                session.flush();
            }
            //}
        } catch (Exception ex) {
            error = true;
        }

        tx.commit();

        session.close();

        if (result.size()
                > 0 && !error) {
            RawAccount[] raw_accounts = result.toArray(new RawAccount[result.size()]);
            return new MessageCarrier("RAWUSER", raw_accounts);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    private MessageCarrier handleDetailTaskRequest(MessageCarrier mc) {
        int num = (Integer) mc.getObj();

        List<RawAccount> result = new ArrayList<>();
        Session session = DatabaseManager.getSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createSQLQuery("SELECT `AUTO_INCREMENT` "
                + "FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'ylproj' "
                + "AND TABLE_NAME = 'raw_account';");

        long count = new Long(q.list().get(0).toString());
        long range = (count > 100) ? 100 : count - 1;

        double pick = Math.random();
        long lower = (long) (pick * count);
        long upper = lower + range;

        boolean error = false;
        try {
            List<RawAccount> items;
            items = session.createCriteria(RawAccount.class)
                    .add(Restrictions.gt("id", new Long(lower)))
                    .add(Restrictions.le("id", new Long(upper)))
                    .add(Restrictions.eq("crawlstate", 0))
                    .addOrder(Order.asc("uid"))
                    .setMaxResults(num)
                    .list();

            RawAccount item;
            /*
             for (int i = 0; i < num; i++) {

                
             do {
             item = (RawAccount) session
             .createCriteria(RawAccount.class)
             .add(Restrictions.eq("id", new Long((long) (count - Math.random() * range))))
             .uniqueResult();
             } while (item == null || item.getCrawlstate() == 1);
             */
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);
                result.add(item);
            }
            //}
        } catch (Exception ex) {
            error = true;
        }

        tx.commit();

        session.close();

        if (result.size()
                > 0 && !error) {
            RawAccount[] raw_accounts = result.toArray(new RawAccount[result.size()]);
            return new MessageCarrier("RAWUSER", raw_accounts);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    private MessageCarrier handleRawUser(MessageCarrier mc) {
        RawAccount[] rusers = (RawAccount[]) mc.getObj();

        Buffer<RawAccount> buffer = connector.getBufferStore().use("rawusers");
        
        for (int i=0;i<rusers.length;i++){
            connector.blockedpush(buffer, rusers[i]);
        }

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
            case "DetailTask":
                reply=handleDetailTaskRequest(mc);
                break;
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
