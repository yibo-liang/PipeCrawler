/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import ppc.dynamic.BlockAnalyser;
import ppc.dynamic.BlockAnalysisResult;
import ppc.dynamic.SectionAnalysisResult;
import ppc.interfaceclass.Buffer;
import ppc.interfaceclass.Worker;

/**
 *
 * @author Yibo
 */
public class SectionManager {

    private final Buffer InputBuffer;
    private final Buffer OutputBuffer;
    private final Class workerclass;
    private final Class pipeblockclass;

    private final HashMap<Integer, BlockAnalyser> analysers;
    private final HashMap<Integer, Pipeblock> pipeblocks;

//each block manager has a threadpool
    ThreadPoolExecutor threadPoolExecutor;

    private int corePoolSize = 500;
    private int maxPoolSize = 500;
    private long keepAliveTime = 5000;
    private int threadNumber_initial = 10;
    private int threadNumber_total = 0;
    private int threadNumber_pause = 0;
    private int threadNumber_running = 0;

    public int getThreadNumber_actual() {
        return threadNumber_total;
    }

    public synchronized void setThreadNumber_Plan(int threadNumber_Plan) {
        this.threadNumber_initial = threadNumber_Plan;
    }

    public synchronized void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public synchronized void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public synchronized void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public SectionManager(Class pipeblockclass, Class workerclass, Buffer InputBuffer, Buffer OutBuffer) {
        this.pipeblocks = new HashMap<>();
        this.analysers = new HashMap<>();
        this.InputBuffer = InputBuffer;
        this.OutputBuffer = OutBuffer;
        this.workerclass = workerclass;
        this.pipeblockclass = pipeblockclass;

    }

    public synchronized void BlockStart() {
        threadNumber_total = 0;
        threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
        for (Integer i = 1; i <= threadNumber_initial; i++) {

            addPipeBlock(i);

        }
    }

    private synchronized void addPipeBlock(int index) {
        try {

            //following code creates a worker and an analyser class for each
            //pipeline block class.
            // Analysers and threads of Blocks are saved into a hashmap for future reference.
            //pipeline block is then executed in the threadpool
            BlockAnalyser a = new BlockAnalyser();
            analysers.put(index, a);
            Constructor workerConstructor = workerclass.getConstructor(Buffer.class, Buffer.class);
            Worker w = (Worker) workerConstructor.newInstance(InputBuffer, OutputBuffer);

            Constructor pbConstructor;
            pbConstructor = pipeblockclass.getConstructor(Worker.class, Buffer.class, Buffer.class);
            Pipeblock pb;
            pb = (Pipeblock) pbConstructor.newInstance(w, InputBuffer, OutputBuffer);
            pipeblocks.put(index, pb);

            threadPoolExecutor.execute(pb);
            threadNumber_total++;
            threadNumber_running++;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(SectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(SectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized void ReducePipesBy(int n) {
        int running = threadNumber_running;
        //if n is larger or equal to running thread, pause all.
        for (int i = running; i >= running - n && i >= 1; i--) {
            pipeblocks.get(i).setPausing(true);
            threadNumber_pause++;
            threadNumber_running--;
        }
    }

    public synchronized void IncreasePipesBy(int n) {
        for (int i = 1; i <= n; i++) {
            if (threadNumber_running < threadNumber_total) {
                pipeblocks.get(threadNumber_running+1).notify();
            }else{
                addPipeBlock(i+threadNumber_total);
            }
        }
    }

    public synchronized SectionAnalysisResult GetSectionAnalyseResult() {
        //calculate everything from analysers
        //reset all analysers
        SectionAnalysisResult result = new SectionAnalysisResult();
        long maxLatency = Long.MIN_VALUE;
        long minLatency = Long.MAX_VALUE;
        double meanLatency = 0;
        double throughput = 0;
        int workdone = 0;

        for (int i = 1; i <= threadNumber_total; i = i + 1) {
            BlockAnalysisResult temp = analysers.get(i).analyse();
            if (temp.getMaximumLatency() > maxLatency) {
                maxLatency = temp.getMaximumLatency();
            }
            if (temp.getMinimumLatency() < minLatency) {
                minLatency = temp.getMinimumLatency();
            }

            workdone += temp.getWorkdoneAmount();

            Pipeblock pi = pipeblocks.get(i);
            if (pi.isRunning()) {
                //running++;
                meanLatency = meanLatency + (temp.getAverageLatency() - meanLatency) / i;
                throughput += temp.getBlockThroughput();
            }
            if (pi.isPausing()) {
                //pausing++;
            }
        }

        result.setRunningThreads(threadNumber_running);
        result.setPausedThreads(threadNumber_pause);
        result.setAverageLatency(meanLatency);
        result.setWorkdoneAmount(workdone);
        result.setSectionThroughput(throughput);

        return result;
    }

}
