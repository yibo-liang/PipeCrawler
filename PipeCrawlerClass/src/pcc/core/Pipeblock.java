/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import ppc.bufferclass.LockedQueueBuffer;
import ppc.dynamic.BlockAnalyser;
import ppc.interfaceclass.Worker;

/**
 *
 * @author yl9
 */
public class Pipeblock implements Runnable {

    private LockedQueueBuffer inputBuffer;
    private LockedQueueBuffer outputBuffer;
    private BlockManager manager;
    private BlockAnalyser analyser;

    private Worker worker;

    private boolean isPausing = false;
    private boolean isRunning = true;

    private long worktimer;

    public boolean isIsPausing() {
        return isPausing;
    }

    public void setIsPausing(boolean isPausing) {
        this.isPausing = isPausing;
    }

    public boolean isIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public synchronized void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void run() {

        synchronized (this) {
            if (analyser != null) {
                analyser.BlockStart();
            }
            while (manager == null || isRunning) {
                // handle pause
                // pause can only happen before a work, not while it is working
                if (isPausing) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Pipeblock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                WorkStart();
                this.worker.work();
                WorkFinish();
            }
        }

    }

    private void WorkStart() {
        worktimer = System.nanoTime();
    }

    private void WorkFinish() {
        long latency = System.nanoTime() - worktimer;
        if (analyser!=null){
            analyser.addSingleWorkLatency(latency);
        }
    }
}
