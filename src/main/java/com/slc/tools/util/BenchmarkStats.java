package com.slc.tools.util;

import java.time.Duration;
import java.util.List;

public record BenchmarkStats(int clockChecks, int loopsBetweenChecks, Duration maxDuration,
                    int loopsCompleted, Duration actualTimeElapsed, Double size, String testName) {

    public long averageTimeNanos() {
        return actualTimeElapsed.toNanos() / loopsCompleted;
    }

    /** Returns a String representation of this object. It's a lot longer than most toString() methods
     * because it's intended solely for printing to System.out.
     * @returns the String representation
     */
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
        sb.append(averageTimeNanos());
        sb.append(" ns \n");

        return sb.toString();
    }

    /**
     * Primarily for unit testing
     * @return Whether all fields are defined and greater than zero
     */
    public boolean isComplete() {
        return clockChecks > 0 && loopsBetweenChecks > 0 && maxDuration != null
                        && loopsCompleted > 0 && actualTimeElapsed != null && size != null
                        && testName != null;
    }

    public static String getTestNameFromList(List<BenchmarkStats> list) throws NullPointerException {
        for (BenchmarkStats stat : list) {
            if (stat != null) {
                return stat.testName();
            }
        }
        throw new NullPointerException("All elements of list are null");
    }

}
