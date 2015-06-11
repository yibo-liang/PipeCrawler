/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.MonitoredLUBuffer;
import jpipe.buffer.LUBuffer;
import jpipe.buffer.util.BufferStore;
import jpipe.core.pipeline.MultiPipeSection;
import jpipe.core.pipeline.DefaultWorkerFactory;
import jpipe.core.pipeline.SinglePipeSection;
import jpipe.interfaceclass.IBuffer;
import jpipe.interfaceclass.IWorkerLazy;
import jpipe.util.Pair;
import jpipe.util.Triplet;
import pcc.http.entity.Proxy;
import pcc.http.util.ProxyMonitor;
import pcc.workers.AccountCrawler;
import pcc.workers.UserPagePusher;
import pcc.workers.Initialiser;
import pcc.workers.ProxySupplier;

/**
 *
 * @author yl9
 */
public class PipeCrawler {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        /*
         LUBuffer<String> buffer;
         buffer = new LUBuffer<>(20);
         IBuffer[] blist = new IBuffer[1];
         blist[0] = buffer;
         SectionAnalyser a1 = new SectionAnalyser();

         SinglePipeSection pb1 = new SinglePipeSection(new AccountCrawler(), blist);
         pb1.setAnalyser(a1);
         (new Thread(pb1)).start();

         for (int i = 1; i <= 10; i++) {
         buffer.push(String.valueOf(i));
         }

         while (true) {
         SectionAnalysisResult bar1 = a1.analyseMs();
         System.out.println(bar1.toString());
         Thread.sleep(1000);
         }
         */
        /*x
         DefaultWorkerFactory<testworker2> factory
         = new DefaultWorkerFactory<>(testworker2.class);
         TPBuffer[] b = new TPBuffer[1];
         b[0] = new LUBuffer(1);
         MultiPipeSection pp = new MultiPipeSection(factory, b, 10);
         pp.Start();
         (new testmaster1()).work(b);

         int i = 1;
         int k = 1;
         */

        LUBuffer<String> containeridbuffer = new LUBuffer<>(20);
        LUBuffer<String> Failedcontaineridbuffer = new LUBuffer<>(20);

        LUBuffer<Triplet<IWorkerLazy, String, String>> pagelistbuffer = new LUBuffer<>(20);
        LUBuffer<Triplet<IWorkerLazy, String, String>> Failedpagelistbuffer = new LUBuffer<>(20);

        LUBuffer<String> usersbuffer = new LUBuffer<>(0);
        LUBuffer<Proxy> proxysbuffer = new LUBuffer<>(5);
        MonitoredLUBuffer<Pair> recycleproxysbuffer
                = new MonitoredLUBuffer<>(100, new ProxyMonitor(120000));

        BufferStore bs1 = new BufferStore();

        bs1.put("containerid", containeridbuffer);
        bs1.put("failedcontainerid", Failedcontaineridbuffer);

        bs1.put("pagelist", pagelistbuffer);
        bs1.put("failedpagelist", Failedpagelistbuffer);

        bs1.put("users", usersbuffer);
        bs1.put("proxys", proxysbuffer);
        bs1.put("recycledproxy", recycleproxysbuffer);

        DefaultWorkerFactory<Initialiser> factory1 = new DefaultWorkerFactory<>(Initialiser.class);
        DefaultWorkerFactory<UserPagePusher> factory2 = new DefaultWorkerFactory<>(UserPagePusher.class);
        DefaultWorkerFactory<AccountCrawler> factory3 = new DefaultWorkerFactory<>(AccountCrawler.class);

        MultiPipeSection cp1 = new MultiPipeSection(factory1, bs1, 1);
        MultiPipeSection cp2 = new MultiPipeSection(factory2, bs1, 50);
        MultiPipeSection cp3 = new MultiPipeSection(factory3, bs1, 50);

        cp1.Start();
        cp2.Start();
        cp3.Start();
        if (CrawlerSetting.USE_PROXY) {
            ProxySupplier ps = new ProxySupplier(25);
            ps.setBufferStore(bs1);
            SinglePipeSection proxySupplier = new SinglePipeSection(ps);

            (new Thread(proxySupplier)).start();
        }

        usersbuffer.push(new Worker() {
            @Override
            public int work() {
                return Worker.SUCCESS;
            }
        }, "1726604697");

        while (true) {
            Thread.sleep(3000);
            System.out.println("=============================\n");
            System.out.println("Proxy pushed: "+proxysbuffer.getItemCount());
            //System.out.println("1 pausing=" + cp1.getThreadNumber_pausing() + ", resting=" + cp1.getThreadNumber_resting());
            //System.out.println("2 pausing=" + cp2.getThreadNumber_pausing() + ", resting=" + cp2.getThreadNumber_resting());
            //System.out.println("3 pausing=" + cp3.getThreadNumber_pausing() + ", resting=" + cp3.getThreadNumber_resting());
            bs1.printBufferState();
            System.out.println("------------------------------");
            System.out.println(cp1.GetSectionAnalyseResult());
            System.out.println(cp2.GetSectionAnalyseResult());
            System.out.println(cp3.GetSectionAnalyseResult());

        }
    }

}
