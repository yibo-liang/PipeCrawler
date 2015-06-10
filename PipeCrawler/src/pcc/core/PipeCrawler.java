/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import jpipe.buffer.MonitoredBufferLocked;
import jpipe.buffer.QBufferLocked;
import jpipe.buffer.util.TPBufferStore;
import jpipe.core.ConcurrentPipes;
import jpipe.core.DefaultWorkerFactory;
import jpipe.core.PipeSection;
import jpipe.interfaceclass.IBUffer;
import jpipe.interfaceclass.IWorker;
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
         QBufferLocked<String> buffer;
         buffer = new QBufferLocked<>(20);
         IBUffer[] blist = new IBUffer[1];
         blist[0] = buffer;
         SectionAnalyser a1 = new SectionAnalyser();

         PipeSection pb1 = new PipeSection(new AccountCrawler(), blist);
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
         b[0] = new QBufferLocked(1);
         ConcurrentPipes pp = new ConcurrentPipes(factory, b, 10);
         pp.Start();
         (new testmaster1()).work(b);

         int i = 1;
         int k = 1;
         */
        QBufferLocked<String> initbuffer = new QBufferLocked<>(20);
        
        QBufferLocked<String> containeridbuffer = new QBufferLocked<>(20);
        QBufferLocked<String> Failedcontaineridbuffer = new QBufferLocked<>(20);
        
        QBufferLocked<Triplet<IWorker, String, String>> pagelistbuffer = new QBufferLocked<>(20);
        QBufferLocked<Triplet<IWorker, String, String>> Failedpagelistbuffer = new QBufferLocked<>(20);
        
        QBufferLocked<String> usersbuffer = new QBufferLocked<>(500);
        QBufferLocked<Proxy> proxysbuffer = new QBufferLocked<>(5);
        MonitoredBufferLocked<Pair> recycleproxysbuffer
                = new MonitoredBufferLocked<>(100, new ProxyMonitor(120000));

        TPBufferStore.put("initbuffer", initbuffer);
        
        TPBufferStore.put("containerid", containeridbuffer);
        TPBufferStore.put("failedcontainerid", Failedcontaineridbuffer);
        
        TPBufferStore.put("pagelist", pagelistbuffer);
        TPBufferStore.put("failedpagelist", Failedpagelistbuffer);
        
        
        TPBufferStore.put("users", usersbuffer);
        TPBufferStore.put("proxys", proxysbuffer);
        TPBufferStore.put("recycledproxy", recycleproxysbuffer);

        DefaultWorkerFactory<Initialiser> factory1 = new DefaultWorkerFactory<>(Initialiser.class);
        DefaultWorkerFactory<UserPagePusher> factory2 = new DefaultWorkerFactory<>(UserPagePusher.class);
        DefaultWorkerFactory<AccountCrawler> factory3 = new DefaultWorkerFactory<>(AccountCrawler.class);

        ConcurrentPipes cp1 = new ConcurrentPipes(factory1, null, 1);
        ConcurrentPipes cp2 = new ConcurrentPipes(factory2, null, 25);
        ConcurrentPipes cp3 = new ConcurrentPipes(factory3, null, 25);

        cp1.Start();
        cp2.Start();
        cp3.Start();
        if (CrawlerSetting.USE_PROXY) {
            PipeSection proxySupplier = new PipeSection(new ProxySupplier(25), null);
            (new Thread(proxySupplier)).start();
        }

        initbuffer.push("1726604697");
    }

}
