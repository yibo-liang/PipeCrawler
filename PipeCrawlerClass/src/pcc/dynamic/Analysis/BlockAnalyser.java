/*
 * The MIT License
 *
 * Copyright 2015 yl9.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pcc.dynamic.Analysis;

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
