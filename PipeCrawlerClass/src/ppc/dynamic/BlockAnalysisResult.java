/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppc.dynamic;

/**
 *
 * @author yl9
 */
public class BlockAnalysisResult {

    private double BlockThroughput;
    private double AverageLatency;
    private long MaximumLatency;
    private long MinimumLatency;
    private int workdoneAmount;
    private long blockRunningTime;

    public long getBlockRunningTime() {
        return blockRunningTime;
    }

    public void setBlockRunningTime(long blockRunningTime) {
        this.blockRunningTime = blockRunningTime;
    }

    public int getWorkdoneAmount() {
        return workdoneAmount;
    }

    public void setWorkdoneAmount(int workdoneAmount) {
        this.workdoneAmount = workdoneAmount;
    }

    public double getBlockThroughput() {
        return BlockThroughput;
    }

    public void setBlockThroughput(double BlockThroughput) {
        this.BlockThroughput = BlockThroughput;
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

}
