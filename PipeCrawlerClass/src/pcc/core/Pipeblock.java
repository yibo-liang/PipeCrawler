/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcc.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import ppc.dynamic.BlockAnalyser;
import ppc.interfaceclass.Buffer;
import ppc.interfaceclass.Worker;

/**
 *
 * @author Yibo
 */
public class Pipeblock implements Runnable {

    private final Buffer inputBuffer;
    private final Buffer outputBuffer;
    private SectionManager manager;
    private BlockAnalyser analyser;

    private Worker worker;

    private boolean Pausing = false;
    private boolean Running = true;

    private long worktimer;

    public boolean isPausing() {
        return Pausing;
    }

    public void setPausing(boolean isPausing) {
        this.Pausing = isPausing;
    }

    public boolean isRunning() {
        return Running;
    }

    public void setRunning(boolean isRunning) {
        this.Running = isRunning;
    }

    public synchronized void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Pipeblock(Worker worker, Buffer InputBuffer, Buffer OutputBuffer) {
        this.inputBuffer = InputBuffer;
        this.outputBuffer = OutputBuffer;
        
    }

    @Override
    public void run() {

        synchronized (this) {
            if (analyser != null) {
                analyser.BlockStart();
            }
            while (manager == null || Running) {
                // handle pause
                // pause can only happen before a work, not while it is working
                if (Pausing) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Pipeblock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                WorkStart();
                this.worker.work(this.inputBuffer, this.outputBuffer);
                WorkFinish();
            }
        }

    }

    private void WorkStart() {
        worktimer = System.nanoTime();
    }

    private void WorkFinish() {
        long latency = System.nanoTime() - worktimer;
        if (analyser != null) {
            analyser.workdone(latency);
        }
    }
}
