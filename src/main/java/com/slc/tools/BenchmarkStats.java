package com.slc.tools;

import java.time.Duration;

public record BenchmarkStats (int innerLoops, int outerLoops, Duration totalDuration,
                    int loopsCompleted, Duration timeElapsed) {
    
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

        int newInnerLoops = stats1.innerLoops + stats2.innerLoops;
        int newOuterLoops = stats1.outerLoops + stats2.outerLoops;
        Duration newTotalDuration = stats1.totalDuration.plus(stats2.totalDuration);
        int newLoopsCompleted = stats1.loopsCompleted + stats2.loopsCompleted;
        Duration newTimeElapsed = stats1.timeElapsed.plus(stats2.timeElapsed);

        return new BenchmarkStats(newInnerLoops, newOuterLoops, newTotalDuration, newLoopsCompleted, newTimeElapsed);
    }
}
