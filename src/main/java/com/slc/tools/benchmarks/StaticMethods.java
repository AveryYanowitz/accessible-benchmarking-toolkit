package com.slc.tools.benchmarks;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.util.BenchmarkStats;

public class StaticMethods {

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
                    mapResult = Tester.singleMethodTest(method, null, streamMember, maxDuration, benchmark.clockFrequency(), 
                                    benchmark.idName(), benchmark.idIsMethod(), testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }
}
