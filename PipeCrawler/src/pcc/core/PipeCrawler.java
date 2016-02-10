/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.LUBuffer;
import jpipe.buffer.util.BufferStore;
import jpipe.core.pipeline.MultiPipeSection;
import jpipe.core.pipeline.DefaultWorkerFactory;
import jpipe.core.pipeline.SinglePipeSection;
import jpipe.interfaceclass.IWorkerLazy;
import jpipe.util.Triplet;
import pcc.http.CrawlerConnectionManager;
import pcc.http.entity.Proxy;
import pcc.workers.AccountCrawler;
import pcc.workers.UserPagePusher;
import pcc.workers.Initialiser;
import pcc.workers.NaiveProxyValidator;
import pcc.workers.ProxySupplier;
import pcc.workers.ProxyValidator;

/**
 *
 * @author yl9
 */
public class PipeCrawler {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    
    private static void UserCrawler() throws InterruptedException{
        
        CrawlerConnectionManager.setMaxConnection(5);
        CrawlerConnectionManager.StartConnectionMonitor();
        LUBuffer<String> containeridbuffer = new LUBuffer<>(15);
        LUBuffer<String> Failedcontaineridbuffer = new LUBuffer<>(200);

        LUBuffer<Triplet<IWorkerLazy, String, String>> pagelistbuffer = new LUBuffer<>(200);
        LUBuffer<Triplet<IWorkerLazy, String, String>> Failedpagelistbuffer = new LUBuffer<>(100);

        LUBuffer<String> usersbuffer = new LUBuffer<>(0);
        LUBuffer<Proxy> rawproxysbuffer = new LUBuffer<>(80);
        LUBuffer<Proxy> proxysbuffer = new LUBuffer<>(150);
        LUBuffer<Proxy> recycleproxysbuffer = new LUBuffer<>(0);

        BufferStore bs1 = new BufferStore();

        bs1.put("containerid", containeridbuffer);
        bs1.put("failedcontainerid", Failedcontaineridbuffer);

        bs1.put("pagelist", pagelistbuffer);
        bs1.put("failedpagelist", Failedpagelistbuffer);

        bs1.put("users", usersbuffer);

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

        usersbuffer.push(new Worker() {
            @Override
            public int work() {
                return Worker.SUCCESS;
            }
        }, "5629952990");

        while (true) {
            Thread.sleep(3000);
            System.out.println("=============================");
         //System.out.println("1 pausing=" + cp1.getThreadNumber_pausing() + ", resting=" + cp1.getThreadNumber_resting());
            //System.out.println("2 pausing=" + cp2.getThreadNumber_pausing() + ", resting=" + cp2.getThreadNumber_resting());
            //System.out.println("3 pausing=" + cp3.getThreadNumber_pausing() + ", resting=" + cp3.getThreadNumber_resting());
            System.out.println(bs1.BufferStates());
            System.out.println("  ----------------------------");
            //System.out.println(cp0.GetSectionAnalyseResult());
            System.out.println(pipsec2.GetSectionAnalyseResult());
            System.out.println(pipsec3.GetSectionAnalyseResult());
            System.out.println(pipsec4.GetSectionAnalyseResult());
            System.out.println("  ----------------------------");
            System.out.println(rawproxysbuffer.getPushingRecordToString());
            System.out.println(proxysbuffer.getPushingRecordToString());
            System.out.println(recycleproxysbuffer.getPushingRecordToString());

            System.out.println("    - - - ");
            System.out.println(proxysbuffer.getPollingRecordToString());

        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        UserCrawler();
    }

}
