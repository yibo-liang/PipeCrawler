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
import org.hibernate.criterion.Restrictions;
import pcc.core.GlobalControll;
import pcc.core.entity.MessageCarrier;
import pcc.core.entity.RawAccount;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.DetailCrawlProgress;
import pcc.core.entity.MBlog;
import pcc.core.entity.MBlogProgress;
import pcc.core.entity.MBlogTask;
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
                    //.addOrder(Order.asc("uid"))
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

    //get raw accounts for details
    private void getMoreRawAccounts(DetailCrawlProgress progress, Session session) throws Exception {
        Buffer<RawAccount> raws = this.connector.getBufferStore().use("rawusers_d");
        long step = progress.getUpper() - progress.getLower() + 1;
        progress.setLower(progress.getUpper() + 1);
        progress.setUpper(progress.getUpper() + step);
        session.save(progress);
        session.flush();

        List<RawAccount> items;
        items = session.createCriteria(RawAccount.class)
                .add(Restrictions.gt("id", new Long(progress.getLower())))
                .add(Restrictions.le("id", new Long(progress.getUpper())))
                .add(Restrictions.eq("crawlstate", 0))
                .list();
        if (items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                connector.blockedpush(raws, items.get(i));
            }

        } else {
            throw new Exception("No Raw Accounts");
        }

    }

    private void getMoreAccounts(MBlogProgress progress, Session session) throws Exception {
        Buffer<AccountDetail> raws = this.connector.getBufferStore().use("account_detail_d");
        long step = progress.getUpper() - progress.getLower() + 1;
        progress.setLower(progress.getUpper() + 1);
        progress.setUpper(progress.getUpper() + step);
        session.save(progress);
        session.flush();

        List<AccountDetail> items;
        items = session.createCriteria(RawAccount.class)
                .add(Restrictions.gt("id", new Long(progress.getLower())))
                .add(Restrictions.le("id", new Long(progress.getUpper())))
                //only get 1/10 account due to time/space limitation
                .add(Restrictions.sqlRestriction("10>rand()*100"))
                .list();
        if (items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                connector.blockedpush(raws, items.get(i));
            }

        } else {
            throw new Exception("No Raw Accounts");
        }

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

    private synchronized MessageCarrier handleDetailTaskRequest(MessageCarrier mc) {
        int num = (Integer) mc.getObj();
        long count;
        String total_str = GlobalControll.VARIABLES.get("rawuser_count");
        Session session = DatabaseManager.getSession();
        Transaction tx = session.beginTransaction();
        List<RawAccount> result = new ArrayList<>();

        //get total counjt
        if (total_str == null) {

            Query q = session.createSQLQuery("SELECT `AUTO_INCREMENT` "
                    + "FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA = 'ylproj' "
                    + "AND TABLE_NAME = 'raw_account';");

            count = new Long(q.list().get(0).toString());
            GlobalControll.VARIABLES.put("rawuser_count", String.valueOf(count));
        } else {
            count = Long.parseLong(total_str);
        }
        DetailCrawlProgress progress;
        //get current progress
        try {
            progress = (DetailCrawlProgress) session
                    .createCriteria(DetailCrawlProgress.class)
                    .add(Restrictions.eq("id", new Integer(0)))
                    .uniqueResult();
            if (progress == null) {
                progress = new DetailCrawlProgress();
                progress.setId(0);
                progress.setLower(0L);
                progress.setUpper(4999L);
                session.saveOrUpdate(progress);
            }
            if (progress.getLower() > count) {
                throw new Exception("All raw user details are crawled.");
            }

        } catch (Exception ex) {
            ServerConnector.logError(ex);
            tx.commit();
            session.close();
            return new MessageCarrier("NULL", "");
        }
        //pull from buffer
        Buffer<RawAccount> raws = this.connector.getBufferStore().use("rawusers_d");
        //RawAccount[] resbuf = new RawAccount[num];
        try {
            for (int i = 0; i < num; i++) {
                Object tmp = raws.poll(connector);

                RawAccount a;
                if (tmp != null) {
                    a = (RawAccount) tmp;
                    result.add(a);
                } else {
                    getMoreRawAccounts(progress, session);
                    a = (RawAccount) raws.poll(connector);
                    if (a != null) {
                        result.add(a);
                    } else {
                        throw new Exception("Unexpected, should have a Raw Account");
                    }

                }
            }
        } catch (Exception ex) {
            ServerConnector.logError(ex);
            tx.commit();
            session.close();
            return new MessageCarrier("NULL", "");
        }

        tx.commit();

        session.close();

        if (result.size() > 0) {
            RawAccount[] raw_accounts = result.toArray(new RawAccount[result.size()]);
            return new MessageCarrier("RAWUSER", raw_accounts);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    private MessageCarrier handleRawUser(MessageCarrier mc) {
        RawAccount[] rusers = (RawAccount[]) mc.getObj();

        Buffer<RawAccount> buffer = connector.getBufferStore().use("rawusers");

        for (int i = 0; i < rusers.length; i++) {
            connector.blockedpush(buffer, rusers[i]);
        }

        return new MessageCarrier("ACK", "");
    }

    private MessageCarrier handleAcountDetail(MessageCarrier mc) {
        AccountDetail[] details = (AccountDetail[]) mc.getObj();

        Buffer<AccountDetail> buffer = connector.getBufferStore().use("account_detail");

        for (int i = 0; i < details.length; i++) {
            connector.blockedpush(buffer, details[i]);
        }

        return new MessageCarrier("ACK", "");
    }

    private MessageCarrier _handleMBlogTaskRequest(MessageCarrier mc) {

        int num = (Integer) mc.getObj();
        long count;
        String total_str = GlobalControll.VARIABLES.get("detail_count");
        Session session = DatabaseManager.getSession();
        Transaction tx = session.beginTransaction();
        List<AccountDetail> result = new ArrayList<>();

        //get total counjt
        if (total_str == null) {

            Query q = session.createSQLQuery("SELECT `AUTO_INCREMENT` "
                    + "FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA = 'ylproj' "
                    + "AND TABLE_NAME = 'account_detail';");

            count = new Long(q.list().get(0).toString());
            GlobalControll.VARIABLES.put("detail_count", String.valueOf(count));
        } else {
            count = Long.parseLong(total_str);
        }
        MBlogProgress progress;
        //get current progress
        try {
            progress = (MBlogProgress) session
                    .createCriteria(MBlogProgress.class)
                    .add(Restrictions.eq("id", new Integer(0)))
                    .uniqueResult();
            if (progress == null) {
                progress = new MBlogProgress();
                progress.setId(0);
                progress.setLower(0L);
                progress.setUpper(4999L);
                session.saveOrUpdate(progress);
            }
            if (progress.getLower() > count) {
                throw new Exception("All raw user details are crawled.");
            }

        } catch (Exception ex) {
            ServerConnector.logError(ex);
            tx.commit();
            session.close();
            return new MessageCarrier("NULL", "");
        }
        //pull from buffer
        Buffer<AccountDetail> accounts = this.connector.getBufferStore().use("account_detail_d");
        //RawAccount[] resbuf = new RawAccount[num];
        try {
            for (int i = 0; i < num; i++) {
                Object tmp = accounts.poll(connector);

                AccountDetail a;
                if (tmp != null) {
                    a = (AccountDetail) tmp;
                    result.add(a);
                } else {
                    getMoreAccounts(progress, session);
                    a = (AccountDetail) accounts.poll(connector);
                    if (a != null) {
                        result.add(a);
                    } else {
                        throw new Exception("Unexpected, should have a Account");
                    }

                }
            }
        } catch (Exception ex) {
            ServerConnector.logError(ex);
            tx.commit();
            session.close();
            return new MessageCarrier("NULL", "");
        }

        tx.commit();

        session.close();

        if (result.size() > 0) {
            MBlogTask[] tasks = new MBlogTask[result.size()];
            for (int i = 0; i < result.size(); i++) {
                MBlogTask t = new MBlogTask();
                t.setUser_id(result.get(i).getUid());
                MBlogTask.SubTaskController st = t.new SubTaskController();
                st.setMax_page_num(1);
                t.setAccount(result.get(i));
                t.setSubtask(st);
                tasks[i] = t;
            }
            return new MessageCarrier("MBlogTasks", tasks);
        } else {
            return new MessageCarrier("NULL", "");
        }
    }

    private MessageCarrier handleMBlogTaskRequest(MessageCarrier mc) {
        return _handleMBlogTaskRequest(mc);
    }

    private MessageCarrier handleMBlog(MessageCarrier mc) {
        MBlogTask[] finished = (MBlogTask[]) mc.getObj();
        for (MBlogTask task : finished) {
            Buffer taskbuffer=this.connector.getBufferStore().use("mblogresult");
            this.connector.blockedpush(taskbuffer, task);
        }
        return null;
    }

    @Override
    public MessageCarrier handleMsg(MessageCarrier mc) {
        String msg = mc.getMsg();
        MessageCarrier reply;
        switch (msg) {
            case "DetailTask":
                reply = handleDetailTaskRequest(mc);
                break;
            case "UserTask":
                reply = handleUserTaskRequest(mc);
                break;
            case "RawUser":
                reply = handleRawUser(mc);
                break;
            case "AccountDetail":
                reply = handleAcountDetail(mc);
                break;
            case "RawProxy":
                reply = handleRawProxy(mc);
                break;
            case "MBlog":
                reply = handleMBlog(mc);
            case "MBlogTask":
                reply = handleMBlogTaskRequest(mc);
                break;
            default:
                reply = new MessageCarrier();
        }

        return reply;
    }

}
