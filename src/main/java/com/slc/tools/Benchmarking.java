package com.slc.tools;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Benchmarking {

    public static <T> BenchmarkStats testStream(Stream<T> dataToTest, Consumer<T> methodToTest) {
        BenchmarkStats totalStats = null;
        
    }
    
    private static <T> BenchmarkStats _singleTest(T object, Consumer<T> consumer, 
                        Duration maxDuration, int numberOfLoops) {

        long maxNanoTime = maxDuration.toNanos();
        int outerLoops, innerLoops;
        if (numberOfLoops < 20) {
            outerLoops = 1;
            innerLoops = numberOfLoops;
        } else {
            outerLoops = 10;
            innerLoops = numberOfLoops / 10;
        }

        int completedLoops = 0;
        long startTime = System.nanoTime();
        for (int i = 0; i < outerLoops; i++) {
            if ((System.nanoTime() - startTime) < maxNanoTime) {
                for (int j = 0; j < innerLoops; j++) {
                    consumer.accept(object);
                    completedLoops++;
                }
            } else {
                break;
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);

        return new BenchmarkStats(innerLoops, outerLoops, maxDuration, completedLoops, elapsedTime);

    }

}
