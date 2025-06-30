package com.slc.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
     * @param numberOfLoops The number of loops to run between tests
     * @param idSource The field or method name from which to derive the run ID
     * @param idIsMethod True if idSource names a method, false if it names a field
     * @param testName The name of the method being tested
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> benchmarkConsumer(Consumer<T> methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int numberOfLoops, 
                                                    String idSource, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        return dataToTest.map((T streamMember) -> 
            _singleTest(methodToTest, streamMember, maxDuration, numberOfLoops, idSource, idIsMethod, testName)
        );
    }

    public static <T> Stream<BenchmarkStats> benchmarkConsumer(Consumer<T> methodToTest, Iterable<T> dataToTest,
                                                    Duration maxDuration, int numberOfLoops, 
                                                    String idSource, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        return benchmarkConsumer(methodToTest, asStream, maxDuration, numberOfLoops, idSource, idIsMethod, testName);
    }

    public static <T> Stream<BenchmarkStats> benchmarkConsumer(Consumer<T> methodToTest, T[] dataToTest,
                                                    Duration maxDuration, int numberOfLoops, 
                                                    String idSource, boolean idIsMethod, String testName)
                                                    throws ReflectiveOperationException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        return benchmarkConsumer(methodToTest, asStream, maxDuration, numberOfLoops, idSource, idIsMethod, testName);
    }

    
    private static <T> BenchmarkStats _singleTest(Consumer<T> consumer, T object,  
                                    Duration maxDuration, int numberOfLoops, 
                                    String propertyName, boolean idIsMethod, String testName) {
        long maxNanoTime = maxDuration.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;

        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < maxNanoTime
        && completedLoops <= Integer.MAX_VALUE) {
            clockChecks++;
            for (int i = 0; i < numberOfLoops; i++) {
                consumer.accept(object);
                if (++completedLoops == Integer.MAX_VALUE) {
                    break;
                }
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;

        clockChecks++; // last check returned false, so it didn't increment
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);
        String id = _getPropertyByName(object, propertyName, idIsMethod);
        return new BenchmarkStats(clockChecks, numberOfLoops, maxDuration, completedLoops, elapsedTime, id, testName);
    }

    private static <T> String _getPropertyByName(T object, String propertyName, boolean searchMethods) {
        String value;
        try {
            if (searchMethods) {            
                Method method = object.getClass().getMethod(propertyName);
                method.setAccessible(true);
                value = method.invoke(object).toString();
            } else {
                Field field = object.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                value = field.get(object).toString();
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
        return isNumber(value) ? value : null;
    }

    private static boolean isNumber(String toCheck) {
        try {
            Integer.parseInt(toCheck);
            return true;
        } catch (NumberFormatException e) {
            // do nothing, move on to the next thing
        }
        try {
            Double.parseDouble(toCheck);
            return true;
        } catch (NumberFormatException e) {
            // do nothing, move on to the next thing
        }
        try {
            Long.parseLong(toCheck);
            return true;
        } catch (NumberFormatException e) {
            // do nothing, move on to the next thing
        }
        try {
            Short.parseShort(toCheck);
            return true;
        } catch (NumberFormatException e) {
            // do nothing, move on to the next thing
        }
        try {
            Float.parseFloat(toCheck);
            return true;
        } catch (NumberFormatException e) {
            // at this point, all number types have been exhausted
            return false;
        }

    }

}
