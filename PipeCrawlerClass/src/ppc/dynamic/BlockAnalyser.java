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
public class BlockAnalyser {

    private int workdoneAmount;
    private long LastLatency;
    private long maxLatency;
    private long minLatency;
    private double meanLatency;
    private long blocktime;
    private long blockStartTime;

    public void BlockStart() {
        blockStartTime = System.nanoTime();
    }

    public long getBlockRunningTime() {
        return System.nanoTime() - blockStartTime;
    }

    public void BlockFinish() {
        blocktime = System.nanoTime() - blockStartTime;
        HardReset();
    }

    public void workdone(long latency) {
        workdoneAmount++;
        if (latency > maxLatency) {
            maxLatency = latency;
        }
        if (latency < minLatency) {
            minLatency = latency;
        }
        meanLatency = meanLatency + ((double) latency - meanLatency) / workdoneAmount;

        SoftReset();
    }

    public void SoftReset() {
        LastLatency = 0;
        maxLatency = Long.MIN_VALUE;
        minLatency = Long.MAX_VALUE;
        meanLatency = 0;
        workdoneAmount = 0;

    }

    public void HardReset() {
        this.SoftReset();
        blockStartTime = System.nanoTime();
    }

    public BlockAnalysisResult analyse() {
        long timespent = this.getBlockRunningTime();
        BlockAnalysisResult result = new BlockAnalysisResult();
        result.setAverageLatency(meanLatency);
        result.setMaximumLatency(maxLatency);
        result.setMinimumLatency(minLatency);
        result.setBlockThroughput(Math.pow(10, 9) / meanLatency);
        result.setWorkdoneAmount(workdoneAmount);
        result.setBlockRunningTime(timespent);
        return result;
        
    }

}
