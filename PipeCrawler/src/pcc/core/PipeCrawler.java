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
import pcc.core.entity.User;
import pcc.http.CrawlerConnectionManager;
import pcc.http.entity.Proxy;
import pcc.workers.client.AccountCrawler;
import pcc.workers.client.ClientObjectSender;
import pcc.workers.client.UserPagePusher;
import pcc.workers.client.Initialiser;
import pcc.workers.client.NaiveProxyValidator;
import pcc.workers.client.ProxySupplier;
import pcc.workers.client.ProxyValidator;
import pcc.workers.client.SignalListener;
import pcc.workers.client.SignalSender;
import pcc.workers.server.ServerObjectReceiver;

/**
 *
 * @author yl9
 */
public class PipeCrawler {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    private static void ClientUserCrawler() throws InterruptedException {

        CrawlerConnectionManager.setMaxConnection(1000);
        CrawlerConnectionManager.StartConnectionMonitor();
        LUBuffer<String> containeridbuffer = new LUBuffer<>(15);
        LUBuffer<String> Failedcontaineridbuffer = new LUBuffer<>(200);

        LUBuffer<Triplet<IWorkerLazy, String, String>> pagelistbuffer = new LUBuffer<>(200);
        LUBuffer<Triplet<IWorkerLazy, String, String>> Failedpagelistbuffer = new LUBuffer<>(100);

        LUBuffer<User> resultUserbuffer = new LUBuffer<>(0);
        LUBuffer<User> initUserbuffer = new LUBuffer<>(0);

        LUBuffer<Proxy> rawproxysbuffer = new LUBuffer<>(80);
        LUBuffer<Proxy> proxysbuffer = new LUBuffer<>(150);
        LUBuffer<Proxy> recycleproxysbuffer = new LUBuffer<>(0);

        BufferStore bs1 = new BufferStore();

        bs1.put("containerid", containeridbuffer);
        bs1.put("failedcontainerid", Failedcontaineridbuffer);

        bs1.put("pagelist", pagelistbuffer);
        bs1.put("failedpagelist", Failedpagelistbuffer);

        bs1.put("users", resultUserbuffer);

        bs1.put("initusers", initUserbuffer);

        bs1.put("rawproxys", rawproxysbuffer);
        bs1.put("proxys", proxysbuffer);
        bs1.put("recycledproxy", recycleproxysbuffer);

        DefaultWorkerFactory<ProxyValidator> ProxyValidatorFactory = new DefaultWorkerFactory<>(ProxyValidator.class);
        DefaultWorkerFactory<Initialiser> InitFacotry = new DefaultWorkerFactory<>(Initialiser.class);
        DefaultWorkerFactory<UserPagePusher> PagePusherFactory = new DefaultWorkerFactory<>(UserPagePusher.class);
        DefaultWorkerFactory<AccountCrawler> CrawlerFactory = new DefaultWorkerFactory<>(AccountCrawler.class);

        MultiPipeSection pipsec1 = new MultiPipeSection(InitFacotry, bs1, 5);
        MultiPipeSection pipsec2 = new MultiPipeSection(ProxyValidatorFactory, bs1, 10);
        MultiPipeSection pipsec3 = new MultiPipeSection(PagePusherFactory, bs1, 5);
        MultiPipeSection pipsec4 = new MultiPipeSection(CrawlerFactory, bs1, 20);

        pipsec1.Start();
        pipsec3.Start();
        pipsec4.Start();
        if (CrawlerSetting.USE_PROXY) {
            ProxySupplier ps = new ProxySupplier(10);
            ps.setBufferStore(bs1);
            SinglePipeSection proxySupplier = new SinglePipeSection(ps);

            pipsec2.Start();
            (new Thread(proxySupplier)).start();
        }

        //sender
        ClientObjectSender<User> cos = new ClientObjectSender<>("users");
        cos.setBufferStore(bs1);
        SinglePipeSection senderPip = new SinglePipeSection(cos);
        (new Thread(senderPip)).start();

        initUserbuffer.push(new Worker() {
            @Override
            public int work() {
                return Worker.SUCCESS;
            }
        }, (new User(5629952990L)));

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
            System.out.println(rawproxysbuffer.getPushingRecordToString());
            System.out.println(proxysbuffer.getPushingRecordToString());
            System.out.println(recycleproxysbuffer.getPushingRecordToString());

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
        LUBuffer<User> resultUserbuffer = new LUBuffer<>(0);
        //
        bs1.put("users", resultUserbuffer);

        //create worker - receiver
        ServerObjectReceiver<User> sor = new ServerObjectReceiver<>("users", bs1);

        //create pip section
        SinglePipeSection userReceivePip = new SinglePipeSection(sor);
        //start
        (new Thread(userReceivePip)).start();

        while (true) {
            Thread.sleep(3000);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            System.out.println(sdf.format(cal.getTime()));
            System.out.println(bs1.BufferStates());
            System.out.println("----------------------------");
            if (GlobalControll.getState() == GlobalControll.STOPPING) {
                break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args[0].toUpperCase().equals("USERCRAWLER")) {
            SignalListener sl = (new SignalListener());
            (new Thread(sl)).start();
            ClientUserCrawler();

        } else if (args[0].toUpperCase().equals("SERVER")) {
            SignalListener sl = (new SignalListener());
            (new Thread(sl)).start();
            ServerCrawler();

        } else if (args[0].toUpperCase().equals("STOP")) {
            SignalSender ss = (new SignalSender());
            (new Thread(ss)).start();

        }

    }

}
