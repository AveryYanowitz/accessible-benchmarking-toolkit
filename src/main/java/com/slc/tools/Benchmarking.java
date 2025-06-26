package com.slc.tools;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Benchmarking {

    /**
     * Takes a Stream of objects and time-tests each of them,
     * returning a new Stream of the results
     * @param <T> The type of the object to be tested
     * @param methodToTest An algorithm to test
     * @param dataToTest A Stream of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param numberOfLoops The maximum number of loops to test
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> testStream(Consumer<T> methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int numberOfLoops, String testID) {
        return dataToTest.map((T streamMember) -> 
            _singleTest(methodToTest, streamMember, maxDuration, numberOfLoops, testID)
        );        
    }
    
    private static <T> BenchmarkStats _singleTest(Consumer<T> consumer, T object,  
                                    Duration maxDuration, int numberOfLoops, String testID) {

        long maxNanoTime = maxDuration.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;
        long startTime = System.nanoTime();
        while (true) {
            clockChecks++;
            if ((System.nanoTime() - startTime) < maxNanoTime) {
                for (int i = 0; i < numberOfLoops; i++) {
                    consumer.accept(object);
                    completedLoops++;
                }
            } else {
                break;
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);

        return new BenchmarkStats(clockChecks, numberOfLoops, maxDuration, completedLoops, elapsedTime, testID);

    }

}
