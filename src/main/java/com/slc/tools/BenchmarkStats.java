package com.slc.tools;

import java.time.Duration;

public record BenchmarkStats (int clockChecks, int loopsBetweenChecks, Duration maxDuration,
                    int loopsCompleted, Duration actualTimeElapsed) {
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Clock Checks:             ");
        sb.append(clockChecks);
        sb.append("\n");

        sb.append("Loops Between Checks:     ");
        sb.append(loopsBetweenChecks);
        sb.append("\n");

        sb.append("Maximum Duration Set:     ");
        sb.append(_formatDuration(maxDuration));
        sb.append("\n");

        sb.append("Total Loops Completed:    ");
        sb.append(loopsCompleted);
        sb.append("\n");

        sb.append("Total Time Elapsed:       ");
        sb.append(_formatDuration(actualTimeElapsed));
        sb.append("\n");

        return sb.toString();
    }
    
    private static String _formatDuration(Duration duration) {
        String fullStr = duration.toString(); // has extra chars we don't want
        String numberOnly = fullStr.substring(2, fullStr.length() - 1);
        String secs = " sec";
        if (!numberOnly.equals("1")) {
            secs += "s";
        }
        return numberOnly + secs;
    }
    
    /** Currently unused; allows multiple BenchmarkStats objects to be summed */
    public static BenchmarkStats add(BenchmarkStats stats1, BenchmarkStats stats2) {
        if (stats1 == null) {
            if (stats2 != null) {
                return stats2;
            } else {
                throw new IllegalArgumentException("Cannot add two null records");
            }
        } else if (stats2 == null) {
            return stats1;
        }

        int clockChecksSum = stats1.clockChecks + stats2.clockChecks;
        int loopsBetweenSum = stats1.loopsBetweenChecks + stats2.loopsBetweenChecks;
        Duration totalDurationSum = stats1.maxDuration.plus(stats2.maxDuration);
        int completedLoopsSum = stats1.loopsCompleted + stats2.loopsCompleted;
        Duration timeElapsedSum = stats1.actualTimeElapsed.plus(stats2.actualTimeElapsed);

        return new BenchmarkStats(clockChecksSum, loopsBetweenSum, totalDurationSum, completedLoopsSum, timeElapsedSum);
    }
}
