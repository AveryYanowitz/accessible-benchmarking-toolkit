package com.slc.tools.benchmarks;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class BenchmarkingFuncs {

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
            _singleConsumerTest(methodToTest, streamMember, maxDuration, clockFrequency, idName, idIsMethod, testName)
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
    public static <T> Stream<BenchmarkStats> benchmarkStaticMethod(Method methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency, 
                                                    String idName, boolean idIsMethod, String testName) {
        return dataToTest.map((T streamMember) -> 
            {
                BenchmarkStats mapResult;
                try {
                    mapResult = _singleMethodTest(methodToTest, null, streamMember, maxDuration, clockFrequency, idName, idIsMethod, testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }

    public static <T> Stream<BenchmarkStats> benchmarkInstanceMethod(Method methodToTest, Object target, Stream<T> dataToTest,
                                                    Duration maxDuration, int clockFrequency,
                                                    String idName, boolean idIsMethod, String testName) {
        return dataToTest.map((T streamMember) -> 
            {
                BenchmarkStats mapResult;
                try {
                    mapResult = _singleMethodTest(methodToTest, target, streamMember, maxDuration, clockFrequency, idName, idIsMethod, testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }
    
    private static <T> BenchmarkStats _singleConsumerTest(Consumer<T> consumer, T object,  
                                    Duration maxDuration, int clockFrequency, 
                                    String propertyName, boolean idIsMethod, String testName) {
        long maxNanoTime = maxDuration.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;

        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < maxNanoTime
        && completedLoops <= Integer.MAX_VALUE) {
            clockChecks++;
            for (int i = 0; i < clockFrequency; i++) {
                consumer.accept(object);
                if (++completedLoops == Integer.MAX_VALUE) {
                    break;
                }
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;

        clockChecks++; // last check returned false, so it didn't increment
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);
        Double id = FormatUtils.getPropertyByName(object, propertyName, idIsMethod);
        return new BenchmarkStats(clockChecks, clockFrequency, maxDuration, completedLoops, elapsedTime, id, testName);
    }

    private static <T> BenchmarkStats _singleMethodTest(Method method, Object target, T input,  
                                    Duration maxDuration, int clockFrequency, 
                                    String propertyName, boolean idIsMethod, String testName) 
                                    throws ReflectiveOperationException {
        long maxNanoTime = maxDuration.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;

        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < maxNanoTime
                    && completedLoops <= Integer.MAX_VALUE) {
            clockChecks++;
            for (int i = 0; i < clockFrequency; i++) {
                method.invoke(target, input);
                // increment completedLoops before asking if max has been reached
                // to prevent extra loop from being performed and overflowing
                if (++completedLoops == Integer.MAX_VALUE) {
                    break;
                }
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;

        clockChecks++; // last check returned false, so it didn't increment
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);
        Double id = FormatUtils.getPropertyByName(input, propertyName, idIsMethod);
        return new BenchmarkStats(clockChecks, clockFrequency, maxDuration, completedLoops, elapsedTime, id, testName);
    }

}
