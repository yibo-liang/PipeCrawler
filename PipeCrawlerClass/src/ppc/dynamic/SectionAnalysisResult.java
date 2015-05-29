/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppc.dynamic;

/**
 *
 * @author Yibo
 */
public class SectionAnalysisResult {

    private double SectionThroughput;
    private int RunningThreads;
    private int PausedThreads;
    private double AverageLatency;
    private long MaximumLatency;
    private long MinimumLatency;
    private int workdoneAmount;

    public double getSectionThroughput() {
        return SectionThroughput;
    }

    public void setSectionThroughput(double SectionThroughput) {
        this.SectionThroughput = SectionThroughput;
    }

    public int getRunningThreads() {
        return RunningThreads;
    }

    public void setRunningThreads(int RunningThreads) {
        this.RunningThreads = RunningThreads;
    }

    public int getPausedThreads() {
        return PausedThreads;
    }

    public void setPausedThreads(int PausedThreads) {
        this.PausedThreads = PausedThreads;
    }

    public double getAverageLatency() {
        return AverageLatency;
    }

    public void setAverageLatency(double AverageLatency) {
        this.AverageLatency = AverageLatency;
    }

    public long getMaximumLatency() {
        return MaximumLatency;
    }

    public void setMaximumLatency(long MaximumLatency) {
        this.MaximumLatency = MaximumLatency;
    }

    public long getMinimumLatency() {
        return MinimumLatency;
    }

    public void setMinimumLatency(long MinimumLatency) {
        this.MinimumLatency = MinimumLatency;
    }

    public int getWorkdoneAmount() {
        return workdoneAmount;
    }

    public void setWorkdoneAmount(int workdoneAmount) {
        this.workdoneAmount = workdoneAmount;
    }
    
    
}
