/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import jpipe.buffer.util.BufferStore;
import jpipe.core.pipeline.MultiPipeSection;
import jpipe.core.pipeline.DefaultWorkerFactory;
import jpipe.core.pipeline.SinglePipeSection;
import jpipe.interfaceclass.IWorkerLazy;
import jpipe.util.Triplet;
import pcc.core.entity.MessageCarrier;
import pcc.core.entity.RawAccount;
import pcc.core.hibernate.DatabaseManager;
import pcc.http.CrawlerConnectionManager;
import pcc.http.entity.Proxy;
import pcc.workers.client.rawuser.AccountCrawler;
import pcc.workers.client.common.ClientConnector;
import pcc.workers.client.rawuser.UserPagePusher;
import pcc.workers.client.rawuser.Initialiser;
import pcc.workers.server.ProxySupplier;
import pcc.workers.client.common.ProxyValidator;
import pcc.workers.client.common.SignalListener;
import pcc.workers.client.common.SignalSender;
import pcc.workers.client.rawuser.UserResultCollector;
import pcc.workers.server.ServerConnector;
import pcc.workers.server.common.ServerDisplay;
import pcc.workers.server.common.ServerProtocol;

/**
 *
 * @author yl9
 */
public class PipeCrawler {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    private static void ClientRawUserCrawler() throws InterruptedException {

        CrawlerConnectionManager.setMaxConnection(1000);
        CrawlerConnectionManager.StartConnectionMonitor();
        LUBuffer<String> containeridbuffer = new LUBuffer<>(15);
        LUBuffer<String> Failedcontaineridbuffer = new LUBuffer<>(200);

        LUBuffer<Triplet<IWorkerLazy, String, String>> pagelistbuffer = new LUBuffer<>(200);
        LUBuffer<Triplet<IWorkerLazy, String, String>> Failedpagelistbuffer = new LUBuffer<>(100);

        LUBuffer<RawAccount> rawUserBuffer = new LUBuffer<>(0);
        LUBuffer<RawAccount> initUserbuffer = new LUBuffer<>(0);

        LUBuffer<Proxy> proxysbuffer = new LUBuffer<>(20);

        LUBuffer<Proxy> rawproxysbuffer = new LUBuffer<>(20);
        //Message Buffer
        LUBuffer<ClientConnector.IClientProtocol> messageBuffer = new LUBuffer<>(0);

        BufferStore bs1 = new BufferStore();

        bs1.put("msg", messageBuffer);

        bs1.put("containerid", containeridbuffer);
        bs1.put("failedcontainerid", Failedcontaineridbuffer);

        bs1.put("pagelist", pagelistbuffer);
        bs1.put("failedpagelist", Failedpagelistbuffer);

        bs1.put("initusers", initUserbuffer);

        bs1.put("rawusers", rawUserBuffer);

        bs1.put("rawproxies", rawproxysbuffer);

        bs1.put("proxys", proxysbuffer);

        DefaultWorkerFactory<ProxyValidator> ProxyValidatorFactory = new DefaultWorkerFactory<>(ProxyValidator.class);
        DefaultWorkerFactory<Initialiser> InitFacotry = new DefaultWorkerFactory<>(Initialiser.class);
        DefaultWorkerFactory<UserPagePusher> PagePusherFactory = new DefaultWorkerFactory<>(UserPagePusher.class);
        DefaultWorkerFactory<AccountCrawler> CrawlerFactory = new DefaultWorkerFactory<>(AccountCrawler.class);

        MultiPipeSection pipsec1 = new MultiPipeSection(InitFacotry, bs1, 5);
        MultiPipeSection pipsec2 = new MultiPipeSection(ProxyValidatorFactory, bs1, 10);
        MultiPipeSection pipsec3 = new MultiPipeSection(PagePusherFactory, bs1, 5);
        MultiPipeSection pipsec4 = new MultiPipeSection(CrawlerFactory, bs1, 80);

        pipsec1.Start();

        if (CrawlerSetting.USE_PROXY) {
            pipsec2.Start();
        }
        pipsec3.Start();
        pipsec4.Start();

        //the section that collects all raw user and create upload massage
        UserResultCollector collector = new UserResultCollector();
        collector.setBufferStore(bs1);
        SinglePipeSection collectorSec = new SinglePipeSection(collector);
        (new Thread(collectorSec)).start();

        //client side message sender
        ClientConnector cos = new ClientConnector("msg");
        cos.setBufferStore(bs1);
        SinglePipeSection connectorSec = new SinglePipeSection(cos);
        (new Thread(connectorSec)).start();

        while (true) {
            Thread.sleep(3000);
            System.out.println("=============================");
            //System.out.println("1 pausing=" + cp1.getThreadNumber_pausing() + ", resting=" + cp1.getThreadNumber_resting());
            //System.out.println("2 pausing=" + cp2.getThreadNumber_pausing() + ", resting=" + cp2.getThreadNumber_resting());
            //System.out.println("3 pausing=" + cp3.getThreadNumber_pausing() + ", resting=" + cp3.getThreadNumber_resting());
            System.out.println(bs1.BufferStates());
            System.out.println("  ----------------------------");
            //System.out.println(cp0.GetSectionAnalyseResult());
            System.out.println(pipsec1.GetSectionAnalyseResult());
            System.out.println(pipsec2.GetSectionAnalyseResult());
            System.out.println(pipsec4.GetSectionAnalyseResult());
            System.out.println("  ----------------------------");
            System.out.println(proxysbuffer.getPushingRecordToString());

            System.out.println("    - - - ");
            System.out.println(proxysbuffer.getPollingRecordToString());

            if (GlobalControll.getState() == GlobalControll.STOPPING) {
                break;
            }
        }
        System.exit(0);
    }

    public static void ServerCrawler() throws InterruptedException {
        //buffer store 
        BufferStore bs1 = new BufferStore();

        //user buffer
        LUBuffer<RawAccount> resultUserbuffer = new LUBuffer<>(0);
        bs1.put("rawusers", resultUserbuffer);

        LUBuffer<Proxy> rawproxysbuffer = new LUBuffer<>(300);
        bs1.put("rawproxies", rawproxysbuffer);
        //create worker - receiver

        ServerConnector serverConeector = new ServerConnector();
        serverConeector.setBufferStore(bs1);

        if (CrawlerSetting.USE_PROXY) {
            ProxySupplier ps = new ProxySupplier(300);
            ps.setBufferStore(bs1);
            SinglePipeSection proxySupplier = new SinglePipeSection(ps);
            (new Thread(proxySupplier)).start();
        }

        //create pip section
        SinglePipeSection userReceivePip = new SinglePipeSection(serverConeector);
        //start
        (new Thread(userReceivePip)).start();
        String suffix = "";
        while (true) {
            Thread.sleep(1000);
            Calendar cal = Calendar.getInstance();
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            //System.out.println(sdf.format(cal.getTime()));
            String t = bs1.BufferStates();
            if (!t.equals(suffix)) {
                suffix = t;
                ServerDisplay.changeSuffix(suffix);
            }

            if (ServerDisplay.isUpdated()){
                ServerDisplay.show();
            }
            //System.out.println("----------------------------");
            if (GlobalControll.getState() == GlobalControll.STOPPING) {
                break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String arg = args[0].toUpperCase();
        GlobalControll.PROCESS_TASK = arg;
        switch (arg) {
            case "USERCRAWLER": {
                SignalListener sl = (new SignalListener());
                (new Thread(sl)).start();
                ClientRawUserCrawler();
                break;
            }
            case "SERVER": {
                SignalListener sl = (new SignalListener());
                (new Thread(sl)).start();
                ServerCrawler();
                break;
            }
            case "STOP":
                SignalSender ss = (new SignalSender());
                (new Thread(ss)).start();
                break;
            case "DBINIT":
                DatabaseManager.DBInterface dbi = new DatabaseManager.DBInterface();
                RawAccount[] as = new RawAccount[4];
                as[0] = new RawAccount(5623352990L);
                as[1] = new RawAccount(5135808743L);
                as[2] = new RawAccount(5666578644L);
                as[3] = new RawAccount(3807667648L);
                dbi.Insert(as);
                break;
        }

    }

}
