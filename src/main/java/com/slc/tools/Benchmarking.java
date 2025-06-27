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
     * @return A new Stream containing the results of the tests in the order provided
     */
    public static <T> Stream<BenchmarkStats> benchmarkConsumer(Consumer<T> methodToTest, Stream<T> dataToTest,
                                                    Duration maxDuration, int numberOfLoops, 
                                                    String idSource, boolean idIsMethod)
                                                    throws ReflectiveOperationException {
        return dataToTest.map((T streamMember) -> 
            _singleTest(methodToTest, streamMember, maxDuration, numberOfLoops, idSource, idIsMethod)
        );
    }
    
    private static <T> BenchmarkStats _singleTest(Consumer<T> consumer, T object,  
                                    Duration maxDuration, int numberOfLoops, 
                                    String propertyName, boolean idIsMethod) {
        long maxNanoTime = maxDuration.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;

        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < maxNanoTime) {
            clockChecks++;
            for (int i = 0; i < numberOfLoops; i++) {
                consumer.accept(object);
                completedLoops++;
            }
        }
        long elapsedRaw = System.nanoTime() - startTime;
        
        clockChecks++; // last check returned false, so it didn't increment
        Duration elapsedTime = Duration.ofNanos(elapsedRaw);
        String id = _getPropertyByName(object, propertyName, idIsMethod);
        return new BenchmarkStats(clockChecks, numberOfLoops, maxDuration, completedLoops, elapsedTime, id);
    }

    private static <T> String _getPropertyByName(T object, String propertyName, boolean searchMethods) {
        StringBuilder id = new StringBuilder("Covariate: ");
        try {
            if (searchMethods) {            
                Method method = object.getClass().getMethod(propertyName);
                method.setAccessible(true);
                id.append(method.invoke(object).toString());
            } else {
                Field field = object.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                id.append(field.get(object).toString());
            }
        } catch (ReflectiveOperationException e) {
            id.append("None found");
        }
        return id.toString();
    }

}
