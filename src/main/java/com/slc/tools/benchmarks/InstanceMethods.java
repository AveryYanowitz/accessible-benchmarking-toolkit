package com.slc.tools.benchmarks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.util.BenchmarkStats;


public class InstanceMethods {

    /**
     * Benchmark one method, calling it on the same object each time.
     * @param <C> The class marked with BenchmarkSuite
     * @param <T> The type of data to be fed into the method
     * @param method The method to benchmark
     * @param classAnno The BenchmarkSuite annotation
     * @param target The object the method will be called upon
     * @param dataToTest A list of data to use as parameters for the given method
     * @return A Stream of BenchmarkStats representing the results of the benchmarking
     * @throws InstantiationException If clazz doesn't have an accessible zero-args constructor
     */
    static <C, T> Stream<BenchmarkStats> benchmarkInstanceMethod(Method method, Class<C> clazz, 
                                                                    C target, List<T> dataToTest) throws InstantiationException {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();

        C finalTarget;
        if (target == null) {
            finalTarget = createNewInstance(clazz);
        } else {
            finalTarget = target;
        }

        return dataToTest.stream().map((T streamMember) -> 
            {
                BenchmarkStats mapResult;
                try {
                    mapResult = Tester.singleMethodTest(method, finalTarget, streamMember, maxDuration, 
                    benchmark.clockFrequency(), benchmark.idName(), benchmark.idIsMethod(), testName);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    mapResult = null;
                }
                return mapResult;
            }
        );
    }

    static <T> T createNewInstance(Class<T> clazz) throws InstantiationException {
        String className = clazz.getSimpleName();
        try {
            return clazz.getConstructor().newInstance();
        } catch (IllegalArgumentException | NoSuchMethodException e) {
            // Thrown when wrong arguments are passed to constructor, or no such constructor exists;
            // in this case, it means no zero-args constructor is present and the benchmark was invalid
            throw new InstantiationException(e.getMessage());
        } catch (IllegalAccessException | SecurityException | InstantiationException | InvocationTargetException e) {
            // Other errors don't reflect a problem with the benchmarking annotations,
            // so we won't throw anything, just warn the user
            System.out.print("Warning: Unable to instantiate object of type ");
            System.out.print(className);
            System.out.println("; skipping benchmark");
            return null;
        }
    }

}
