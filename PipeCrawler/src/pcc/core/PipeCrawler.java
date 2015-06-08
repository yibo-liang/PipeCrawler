/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import jpipe.bufferclass.QBufferLocked;
import jpipe.core.ConcurrentPipes;
import jpipe.core.DefaultWorkerFactory;
import jpipe.core.PipeSection;
import jpipe.interfaceclass.IBUffer;
import pcc.workers.AccountCrawler;
import pcc.workers.UserPagePusher;
import pcc.workers.Initialiser;

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
        QBufferLocked<String> pagelistbuffer = new QBufferLocked<>(20);
        QBufferLocked<String> DBInputbuffer = new QBufferLocked<>(20);

        QBufferLocked[] InitSectBuffers = {initbuffer, containeridbuffer};
        QBufferLocked[] PusherBuffers = {containeridbuffer, pagelistbuffer};
        QBufferLocked[] CrawlerBuffers = {pagelistbuffer, DBInputbuffer};

        DefaultWorkerFactory<Initialiser> factory1 = new DefaultWorkerFactory<>(Initialiser.class);
        DefaultWorkerFactory<UserPagePusher> factory2 = new DefaultWorkerFactory<>(UserPagePusher.class);
        DefaultWorkerFactory<AccountCrawler> factory3 = new DefaultWorkerFactory<>(AccountCrawler.class);
        
        ConcurrentPipes cp1=new ConcurrentPipes(factory1, InitSectBuffers, 1);
        ConcurrentPipes cp2=new ConcurrentPipes(factory2, PusherBuffers, 10);
        ConcurrentPipes cp3=new ConcurrentPipes(factory3, CrawlerBuffers, 10);
        
        cp1.Start();
        cp2.Start();
        cp3.Start();

        initbuffer.push("1726604697");
    }

}
