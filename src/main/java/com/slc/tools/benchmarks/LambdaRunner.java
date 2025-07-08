package com.slc.tools.benchmarks;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.FormatUtils;

public class LambdaRunner {
    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results.
     * @param <T> The type of the object to be tested
     * @param methodToTest An algorithm to test
     * @param dataToTest A Stream of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between tests
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> benchmarkConsumable(Consumer<T> methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        return dataToTest.map((T streamMember) -> 
            Tester.singleConsumerTest(methodToTest, streamMember, maxDuration, clockFrequency, idName, idIsMethod, testName)
        );
    }

    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results.
     * @param <T> The type of the object to be tested
     * @param methodToTest An algorithm to test
     * @param dataToTest An Iterable of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between tests
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> benchmarkConsumable(Consumer<T> methodToTest, Iterable<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        return benchmarkConsumable(methodToTest, asStream, maxDuration, clockFrequency, idName, idIsMethod, testName);
    }

    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results.
     * @param <T> The type of the object to be tested
     * @param methodToTest An algorithm to test
     * @param dataToTest An array of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between clock checks
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> benchmarkConsumable(Consumer<T> methodToTest, T[] dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        return benchmarkConsumable(methodToTest, asStream, maxDuration, clockFrequency, idName, idIsMethod, testName);
    }

    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results and discarding return results
     * @param <T> The type of the object to be tested
     * @param <R> The original return type of the function
     * @param methodToTest An algorithm to test
     * @param dataToTest A Stream of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between tests
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T, R> Stream<BenchmarkStats> benchmarkFunction(Function<T, R> methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Consumer<T> asConsumer = FormatUtils.toConsumer(methodToTest);
        return benchmarkConsumable(asConsumer, dataToTest, maxDuration, clockFrequency, idName, idIsMethod, testName);
    }

    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results and discarding return results
     * @param <T> The type of the object to be tested
     * @param <R> The original return type of the function
     * @param methodToTest An algorithm to test
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between tests
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T, R> Stream<BenchmarkStats> benchmarkFunction(Function<T, R> methodToTest, Iterable<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        Consumer<T> asConsumer = FormatUtils.toConsumer(methodToTest);
        return benchmarkConsumable(asConsumer, asStream, maxDuration, clockFrequency, idName, idIsMethod, testName);
    }

    /**
     * Takes a Stream of objects and time-tests each of them, returning a new Stream of the results and discarding return results
     * @param <T> The type of the object to be tested
     * @param <R> The original return type of the function
     * @param methodToTest An algorithm to test
     * @param dataToTest An array of objects which will be passed to the provided method
     * @param maxDuration The longest time any test should run
     * @param clockFrequency The number of loops to run between clock checks
     * @param idName The field or method name from which to derive the run ID
     * @param idIsMethod True if idName names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T, R> Stream<BenchmarkStats> benchmarkFunction(Function<T, R> methodToTest, T[] dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        Consumer<T> asConsumer = FormatUtils.toConsumer(methodToTest);
        return benchmarkConsumable(asConsumer, asStream, maxDuration, clockFrequency, idName, idIsMethod, testName);
    }
    
}
