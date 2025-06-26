package com.slc.tools;

import java.time.Duration;

public record BenchmarkStats (int innerLoops, int outerLoops, Duration totalDuration,
                    int loopsCompleted, Duration timeElapsed) {
    
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

        int innerLoopSum = stats1.innerLoops + stats2.innerLoops;
        int outerLoopSum = stats1.outerLoops + stats2.outerLoops;
        Duration totalDurationSum = stats1.totalDuration.plus(stats2.totalDuration);
        int completedLoopsSum = stats1.loopsCompleted + stats2.loopsCompleted;
        Duration timeElapsedSum = stats1.timeElapsed.plus(stats2.timeElapsed);

        return new BenchmarkStats(innerLoopSum, outerLoopSum, totalDurationSum, completedLoopsSum, timeElapsedSum);
    }
}
