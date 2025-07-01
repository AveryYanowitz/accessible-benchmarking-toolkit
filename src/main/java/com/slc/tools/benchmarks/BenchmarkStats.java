package com.slc.tools.benchmarks;

import java.time.Duration;
import java.util.List;

import lombok.Getter;

@Getter
public class BenchmarkStats {

    private final String testName;
    private final Double size;
    private final int clockChecks, loopsBetweenChecks, loopsCompleted;
    private final Duration maxDuration, actualTimeElapsed;
    private final double averageTimeMillis;

    public BenchmarkStats(int clockChecks, int loopsBetweenChecks, Duration maxDuration,
                    int loopsCompleted, Duration actualTimeElapsed, Double size, String testName) {

        this.testName = testName;
        this.size = size;
        this.clockChecks = clockChecks;
        this.loopsBetweenChecks = loopsBetweenChecks;
        this.maxDuration = maxDuration;
        this.loopsCompleted = loopsCompleted;
        this.actualTimeElapsed = actualTimeElapsed;

        this.averageTimeMillis = (double) actualTimeElapsed.toMillis() / loopsCompleted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Size:                     ");
        sb.append(size);
        sb.append("\n\n");

        sb.append("Clock Checks:             ");
        sb.append(clockChecks);
        sb.append("\n");

        sb.append("Loops Between Checks:     ");
        sb.append(loopsBetweenChecks);
        sb.append("\n");

        sb.append("Maximum Duration Set:     ");
        sb.append(FormatUtils.formatDuration(maxDuration));
        sb.append("\n");

        sb.append("Total Loops Completed:    ");
        sb.append(loopsCompleted);
        sb.append("\n");

        sb.append("Total Time Elapsed:       ");
        sb.append(FormatUtils.formatDuration(actualTimeElapsed));
        sb.append("\n");

        sb.append("Average Time Per Call:    ");
        sb.append(averageTimeMillis);
        sb.append(" ms \n");

        return sb.toString();
    }

    @SafeVarargs
    public static void printStats(List<BenchmarkStats>... results) {
        for (List<BenchmarkStats> result : results) {
            for (BenchmarkStats stats : result) {
                System.out.println(stats);
            }
        }
    }

}
