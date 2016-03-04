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

import java.util.List;
import jpipe.abstractclass.buffer.Buffer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
        this.connector=connector;
    }

    
    
    private MessageCarrier handleUserTaskRequest(MessageCarrier mc){
        int num=(Integer) mc.getObj();
        RawAccount[] raw_accounts=new RawAccount[num];
        Session session=DatabaseManager.getSession();
        Transaction tx=session.beginTransaction();
        for (int i=0;i<num;i++){
            List<RawAccount> items = session
                    .createCriteria(RawAccount.class)
                    .add(Restrictions.eq("crawlstate", new Integer(0)))
                    .setMaxResults(1)
                    .list();
            RawAccount item=items.get(0);
            item.setCrawlstate(1);
            raw_accounts[i]=item;
            session.save(item);
            session.flush();
            session.clear();
            
        }
        tx.commit();
        session.close();
        
        
        
        return new MessageCarrier("TASK", raw_accounts);
    }
    
    private MessageCarrier handleRawUser(MessageCarrier mc) {
        RawAccount[] rusers = (RawAccount[]) mc.getObj();
        
        DatabaseManager.DBInterface dbi=new DatabaseManager.DBInterface();
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

    private MessageCarrier handleRawProxy(MessageCarrier mc){
        Buffer<Proxy> outputBuffer =connector.getBufferStore().use("rawproxies");
        int num=(Integer)mc.getObj();
        Proxy[] ps=new Proxy[num];
        for (int i=0;i<num;i++){
            ps[i]=(Proxy) this.connector.blockedpoll(outputBuffer);
        }
        return new MessageCarrier("rawproxies", ps);
    }
    
    @Override
    public MessageCarrier handleMsg(MessageCarrier mc) {
        String msg = mc.getMsg();
        MessageCarrier reply;
        switch (msg) {
            case "UserTask":
                reply=handleUserTaskRequest(mc);
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
