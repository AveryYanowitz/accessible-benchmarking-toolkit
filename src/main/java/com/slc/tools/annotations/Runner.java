package com.slc.tools.annotations;

import static com.slc.tools.benchmarks.BenchmarkingFuncs.benchmarkMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.benchmarks.Jsonifier;


public class Runner {

    /**
     * Runs all of the `@Benchmarkable` methods written in a given class.
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param classWithBenchmarks The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @return The results of methods with OutputType.RETURN; may be empty
     */
    public static <T> List<BenchmarkStats> runBenchmarks(Class<?> classWithBenchmarks, List<T> dataToTest) 
                                                            throws IOException, IllegalArgumentException {
        BenchmarkSuite classAnno = classWithBenchmarks.getAnnotation(BenchmarkSuite.class);
        if (classAnno != null) {
            return runBenchmarks(classWithBenchmarks, dataToTest, classAnno.outputTo());
        } else {
            return runBenchmarks(classWithBenchmarks, dataToTest, OutputType.JSON);
        }
    }

    /**
     * Runs all of the `@Benchmarkable` methods written in a given class and overrides the class's output type.
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param classWithBenchmarks The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @param outputOverride Overrides a specified OutputType in classWithBenchmarks
     * @return The results of methods with OutputType.RETURN; may be empty
     */
    public static <T> List<BenchmarkStats> runBenchmarks(Class<?> classWithBenchmarks, List<T> dataToTest, OutputType outputOverride) 
                                                        throws IOException, IllegalArgumentException {
        OutputType outputTo = outputOverride;
        String savePath;
        BenchmarkSuite classAnno = classWithBenchmarks.getAnnotation(BenchmarkSuite.class);
        if (classAnno != null) {
            savePath = classAnno.saveLocation() + "/" + classAnno.fileName();
        } else {
            savePath = "src/main/output/results.json";
        }
        Jsonifier jsonifier = new Jsonifier(savePath);

        List<Method> methodsToTest = getBenchmarks(classWithBenchmarks);
        List<BenchmarkStats> resultsList = new ArrayList<>();
        for (Method method : methodsToTest) {
            Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
            Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
            String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();

            Stream<BenchmarkStats> results = benchmarkMethod(method, dataToTest.stream(), maxDuration, benchmark.clockFrequency(),
                                                benchmark.idName(), benchmark.idIsMethod(), testName);

            if (outputTo == OutputType.PRINT) {
                results.forEach((result) -> {
                    System.out.println(result);
                    System.out.println("------");
                });
                System.out.println("------");
                System.out.println("------");
            } else if (outputTo == OutputType.JSON) {
                jsonifier.addToJson(results);
            } else {
                results.forEach(resultsList::add);
            }
        }
        if (jsonifier.size() > 0) {
            jsonifier.jsonify();
        }
        return resultsList;
    }

    public static <T> List<Method> getBenchmarks(Class<T> clazz) {
        Method[] classMethods = clazz.getDeclaredMethods();
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(Benchmarkable.class)
            && Modifier.isStatic(method.getModifiers())
            && method.canAccess(null)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }
}
