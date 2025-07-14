package com.slc.tools.runners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.stream.Stream;

import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.FormatUtils;


public class MethodRunner {

    /**
     * Detects whether the given method is an instance or static method and calls the appropriate test function
     * @param <C> The class that contains <code> method </code>
     * @param <T> The parameter that <code> method </code> takes
     * @param method The method to test
     * @param target If <code> method </code> is an instance method, it will be invoked on target; otherwise, this argument is ignored (and can be null)
     * @param dataToTest A Stream of data to call <code> method </code> with
     * @return A Stream of BenchmarkStats representing the results of calling <code> method </code> on each element of <code> dataToTest </code>
     * @throws IllegalArgumentException When called with invalid method
     */
    static <C, T> Stream<BenchmarkStats> benchmarkMethod(Method method, C target, Stream<T> dataToTest) throws IllegalArgumentException {
        if (Modifier.isStatic(method.getModifiers())) {
            return _benchmarkStaticMethod(method, dataToTest);
        } else {
            return _benchmarkInstanceMethod(method, target, dataToTest);
        }
    }

    /**
     * Detects whether the given method is an instance or static method and calls the appropriate test function.
     * If it's an instance method, then this function will first create an object to invoke it on.
     * @param <C> The class that contains <code> method </code>
     * @param <T> The parameter that <code> method </code> takes
     * @param method The method to test
     * @param dataToTest A Stream of data to call <code> method </code> with
     * @return A Stream of BenchmarkStats representing the results of calling <code> method </code> on each element of <code> dataToTest </code>
     * @throws IllegalArgumentException When called with invalid method
     */
    static <C, T> Stream<BenchmarkStats> benchmarkMethod(Method method, Stream<T> dataToTest) throws IllegalArgumentException {
        if (Modifier.isStatic(method.getModifiers())) {
            return _benchmarkStaticMethod(method, dataToTest);
        } else {
            return _benchmarkInstanceMethod(method, null, dataToTest);
        }
    }

    /**
     * Benchmark one instance method, calling it on the same object each time.
     * @param <C> The class marked with BenchmarkSuite
     * @param <T> The type of data to be fed into <code> method </code> 
     * @param method The method to benchmark
     * @param classAnno The BenchmarkSuite annotation
     * @param target The object <code> method </code> will be called upon
     * @param dataToTest A list of data to use as parameters for <code> method </code>
     * @return A Stream of BenchmarkStats representing the results of calling <code> method </code> on each element of <code> dataToTest </code>
     * @throws IllegalArgumentException when called with invalid or static method
     */
    private static <C, T> Stream<BenchmarkStats> _benchmarkInstanceMethod(Method method, C target, 
                                            Stream<T> dataToTest) throws IllegalArgumentException {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = (benchmark.testName() == null) ? method.getName() : benchmark.testName();
        if (Modifier.isStatic(method.getModifiers()) || !_isValidMethod(method, 1)) {
            throw new IllegalArgumentException(method+" is either static, invalid, or both");
        }                                                
        return dataToTest.map((T streamMember) -> 
            {
                try {
                    if (target == null) {
                        return _singleMethodTest(method, ClassRunner.createNewInstance(method.getDeclaringClass()), streamMember, maxDuration, 
                            benchmark.clockFrequency(), benchmark.idName(), benchmark.idIsMethod(), testName);
                    } else {
                        return _singleMethodTest(method, target, streamMember, maxDuration, 
                            benchmark.clockFrequency(), benchmark.idName(), benchmark.idIsMethod(), testName);
                    }
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        );
    }

    /**
     * Benchmark one valid static method, or throw an IllegalArgumentException is method is invalid
     * @param <T> The type of data to be fed into <code> method </code> 
     * @param method The method to benchmark
     * @param dataToTest A list of data to use as parameters for <code> method </code>
     * @return A Stream of BenchmarkStats representing the results of calling <code> method </code> on each element of <code> dataToTest </code>
     * @throws IllegalArgumentException when called with invalid or non-static method
     */
    private static <T> Stream<BenchmarkStats> _benchmarkStaticMethod(Method method, Stream<T> dataToTest) throws IllegalArgumentException {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();
        if (!Modifier.isStatic(method.getModifiers()) || !_isValidMethod(method, 1)) {
            throw new IllegalArgumentException(method+" is either not static, invalid, or both");
        }
        return dataToTest.map((T streamMember) -> 
            {
                BenchmarkStats mapResult;
                try {
                    mapResult = _singleMethodTest(method, null, streamMember, maxDuration, benchmark.clockFrequency(), 
                                    benchmark.idName(), benchmark.idIsMethod(), testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }

    /**
     * Takes one method, feeds it one input repeatedly, and times how long it takes. Assumes method is "valid" as defined by _isValidMethod()
     * @param <T> The type of the input
     * @param method The method to test
     * @param target The object on which to invoke <code> method </code>. If <code> method </code> is a static method, this is ignored (and should be null.)
     * @param input The input to feed to <code> method </code>
     * @param maxDuration The ideal maximum time for the benchmark to take; note that it may take longer, especially if clockFrequency is high
     * @param clockFrequency How many times to call the consumer between checking if maxDuration has elapsed yet
     * @param propertyName The name of the field or method of object that represents this run's unique ID
     * @param idIsMethod True if property name represents a method, false if it represents a field
     * @param testName An identifier for all the tests of the same consumer
     * @return A single BenchmarkStats object representing the results of this run
     * @throws ReflectiveOperationException
     */

    static <T> BenchmarkStats _singleMethodTest(Method method, Object target, T input,  
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
                try {
                    method.invoke(target, input);
                } catch (ReflectiveOperationException e) {
                    throw e;
                } catch (Exception e) {
                    ClassRunner.printSkipMessage(method, e);
                    System.out.println(input);
                    throw new ReflectiveOperationException(e.getMessage());
                }
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

    /**
     * Checks the provided method to make sure it's accessible and has the correct number of parameters
     * @param method The method to check
     * @param instance Instance of <code> method </code>'s declaring class
     * @param expectedParamCount The number of parameters the method should have
     * @return boolean indicating whether the method is valid
     */
    private static boolean _isValidMethod(Method method, int expectedParamCount) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        boolean paramCountCorrect = method.getParameterCount() == expectedParamCount;
        if (isStatic) {
            return paramCountCorrect && method.canAccess(null);
        } else {
            Object instance = ClassRunner.createNewInstance(method.getDeclaringClass());
            return paramCountCorrect && instance != null && method.canAccess(instance);
        }
    }


}
