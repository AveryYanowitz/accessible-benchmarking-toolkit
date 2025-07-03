package com.slc.tools.annotations;

import static com.slc.tools.benchmarks.BenchmarkingFuncs.benchmarkMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.benchmarks.Jsonifier;


public class Runner {
    public static <T> List<BenchmarkStats> runBenchmarks(Class<T> classWithBenchmarks, Stream<T> dataToTest) throws ReflectiveOperationException, IOException {
        List<Method> methodsToTest = getBenchmarks(classWithBenchmarks);
        List<BenchmarkStats> resultsList = new ArrayList<>();
        for (Method method : methodsToTest) {
            Benchmark benchmark = method.getAnnotation(Benchmark.class);
            Duration maxDuration = Duration.ofNanos(benchmark.maxDurationNanos());
            String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();

            Stream<BenchmarkStats> results = benchmarkMethod(method, dataToTest, maxDuration, benchmark.clockFrequency(),
                                                benchmark.idName(), benchmark.idIsMethod(), testName);

            OutputType output = benchmark.outputTo();            
            if (output == OutputType.PRINT) {
                results.forEach(System.out::println);
            } else if (output == OutputType.JSON) {
                Jsonifier.jsonify(results);
            } else {
                results.forEach(resultsList::add);
            }
        }
        return resultsList;
    }

    public static <T> List<Method> getBenchmarks(Class<T> clazz) {
        Method[] classMethods = clazz.getDeclaredMethods();
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(Benchmark.class)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

}
