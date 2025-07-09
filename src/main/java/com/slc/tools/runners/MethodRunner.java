package com.slc.tools.runners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.FormatUtils;


public class MethodRunner {

    /**
     * Benchmark one method, calling it on the same object each time.
     * @param <C> The class marked with BenchmarkSuite
     * @param <T> The type of data to be fed into the method
     * @param method The method to benchmark
     * @param classAnno The BenchmarkSuite annotation
     * @param target The object the method will be called upon
     * @param dataToTest A list of data to use as parameters for the given method
     * @return A Stream of BenchmarkStats representing the results of the benchmarking
     */
    static <C, T> Stream<BenchmarkStats> benchmarkInstanceMethod(Method method, C target, List<T> dataToTest) {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();

        return dataToTest.stream().map((T streamMember) -> 
            {
                BenchmarkStats mapResult;
                try {
                    mapResult = _singleMethodTest(method, target, streamMember, maxDuration, 
                    benchmark.clockFrequency(), benchmark.idName(), benchmark.idIsMethod(), testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }

    /**
     * Benchmark one method, calling it on the same object each time.
     * @param <T> The type of data to be fed into the method
     * @param method The method to benchmark
     * @param dataToTest A list of data to use as parameters for the given method
     * @return A Stream of BenchmarkStats representing the results of the benchmarking
     */
    static <T> Stream<BenchmarkStats> benchmarkStaticMethod(Method method, List<T> dataToTest) {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Cannot call _benchmarkStatic on a non-static method");
        }
        return dataToTest.stream().map((T streamMember) -> 
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
