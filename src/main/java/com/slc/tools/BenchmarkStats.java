package com.slc.tools;

import java.time.Duration;

import lombok.Getter;

@Getter
public class BenchmarkStats {

    private final int clockChecks, loopsBetweenChecks, loopsCompleted;
    private final double averageTimeMillis;
    private final Duration maxDuration, actualTimeElapsed;
    private final String functionName;

    public BenchmarkStats(int clockChecks, int loopsBetweenChecks, Duration maxDuration,
                    int loopsCompleted, Duration actualTimeElapsed, String functionName) {
        this.clockChecks = clockChecks;
        this.loopsBetweenChecks = loopsBetweenChecks;
        this.maxDuration = maxDuration;
        this.loopsCompleted = loopsCompleted;
        this.actualTimeElapsed = actualTimeElapsed;
        this.functionName = functionName;

        this.averageTimeMillis = loopsCompleted / (double) actualTimeElapsed.toMillis();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functionName);
        sb.append("\n");

        sb.append("Clock Checks:             ");
        sb.append(clockChecks);
        sb.append("\n");

        sb.append("Loops Between Checks:     ");
        sb.append(loopsBetweenChecks);
        sb.append("\n");

        sb.append("Maximum Duration Set:     ");
        sb.append(StringUtils.formatDuration(maxDuration));
        sb.append("\n");

        sb.append("Total Loops Completed:    ");
        sb.append(loopsCompleted);
        sb.append("\n");

        sb.append("Total Time Elapsed:       ");
        sb.append(StringUtils.formatDuration(actualTimeElapsed));
        sb.append("\n");

        sb.append("Average Time Per Call:    ");
        sb.append(averageTimeMillis);
        sb.append(" ms \n");

        return sb.toString();
    }
}
