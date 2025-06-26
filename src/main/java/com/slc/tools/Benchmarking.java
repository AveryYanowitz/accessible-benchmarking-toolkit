package com.slc.tools;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Benchmarking {

    /**
     * Takes a Stream of objects and time-tests each of them,
     * returning a new Stream of the results
     * @param <T> The type of the object to be tested
     * @param dataToTest A Stream of objects
     * @param methodToTest An algorithm to test the provided
     * @param maxDuration The longest time any test should run
     * @param numberOfLoops The maximum number of loops to test
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> testStream(Stream<T> dataToTest, Consumer<T> methodToTest,
                                                    Duration maxDuration, int numberOfLoops) {
        return dataToTest.map((T streamMember) -> {
            return _singleTest(streamMember, methodToTest, maxDuration, numberOfLoops);
        });        
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
