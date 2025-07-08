package com.slc.tools.annotations;

import static com.slc.tools.benchmarks.BenchmarkingFuncs.benchmarkInstanceMethod;
import static com.slc.tools.benchmarks.BenchmarkingFuncs.benchmarkStaticMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ReflectiveOperationException
     */
    public static <T> List<BenchmarkStats> runBenchmarks(Class<?> clazz, List<T> dataToTest) 
                                                            throws IOException, IllegalArgumentException, ReflectiveOperationException {
        BenchmarkSuite classAnno = clazz.getAnnotation(BenchmarkSuite.class);
        if (classAnno != null) {
            return runBenchmarks(clazz, dataToTest, classAnno.outputTo());
        } else {
            return runBenchmarks(clazz, dataToTest, OutputType.JSON);
        }
    }

    /**
     * Runs all of the `@Benchmarkable` methods written in a given class and overrides the class's output type.
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @param outputOverride Overrides a specified OutputType in clazz
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When the given JSON file location is invalid, or the file cannot be written to
     * @throws InstantiationException When non-static benchmarks are present, but no zero-args constructor exists or is visible
     */
    public static <C, T> List<BenchmarkStats> runBenchmarks(Class<C> clazz, List<T> dataToTest, OutputType outputTo) 
                                                        throws IOException, InstantiationException {
        BenchmarkSuite classAnno = clazz.getAnnotation(BenchmarkSuite.class);
        if (classAnno == null) {
            classAnno = DefaultSettings.class.getAnnotation(BenchmarkSuite.class);
        }
        Jsonifier jsonifier = getJsonifier(clazz);

        List<BenchmarkStats> resultsList = new ArrayList<>();
        Frequency whenToInit = classAnno.whenToInstantiate();
        C target = null; // make sure it's init'd
        if (whenToInit == Frequency.ON_INIT) {
            target = _createNewInstance(clazz);
        }
        
        for (Method method : _benchmarkableMethods(clazz)) {
            System.out.println(clazz.getSimpleName() + " " + method.getName());
            Stream<BenchmarkStats> results;
            if (Modifier.isStatic(method.getModifiers())) {
                results = _benchmarkStatic(method, classAnno, dataToTest);
            } else {
                if (whenToInit == Frequency.NEVER) {
                    continue; // can't test non-static methods if we're not allowd to make an instance
                }
                if (whenToInit == Frequency.PER_METHOD) {
                    target = _createNewInstance(clazz);
                }
                results = _benchmarkWithTarget(method, classAnno, target, dataToTest);
            }

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

    private static <T> List<Method> _benchmarkableMethods(Class<T> clazz) throws InstantiationException {
        Method[] classMethods = clazz.getDeclaredMethods();
        System.out.println(clazz.getSimpleName());
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(Benchmarkable.class)
                && !method.isSynthetic()) {
                System.out.println(clazz.getSimpleName() + " " + method.getName());
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    public static Jsonifier getJsonifier(Class<?> clazz) {
        String savePath;
        BenchmarkSuite classAnno = clazz.getAnnotation(BenchmarkSuite.class);
        if (classAnno != null) {
            savePath = classAnno.saveLocation() + "/" + classAnno.fileName();
        } else {
            savePath = "src/main/output/results.json";
        }
        return new Jsonifier(savePath);
    }

    private static <T> T _createNewInstance(Class<T> clazz) throws InstantiationException {
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

    /**
     * Benchmark one method, calling it on the same object each time.
     * @param <C> The class marked with BenchmarkSuite
     * @param method The method to benchmark
     * @param classAnno The BenchmarkSuite annotation
     * @param target The object the method will be called upon
     * @param dataToTest A list of data to use as parameters for the given method
     * @return A Stream of BenchmarkStats representing the results of the benchmarking
     */
    private static <C> Stream<BenchmarkStats> _benchmarkWithTarget(Method method, BenchmarkSuite classAnno, C target, List<?> dataToTest) {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();

        return benchmarkInstanceMethod(method, target, dataToTest.stream(), maxDuration, benchmark.clockFrequency(),
                                        benchmark.idName(), benchmark.idIsMethod(), testName);  
    }

    /**
     * Benchmark one static method
     * @param method The method to benchmark
     * @param classAnno The BenchmarkSuite annotation
     * @param dataToTest A list of data to use as parameters for the given method
     * @return A Stream of BenchmarkStats representing the results of the benchmarking
     */
    private static Stream<BenchmarkStats> _benchmarkStatic(Method method, BenchmarkSuite classAnno, List<?> dataToTest) {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        Duration maxDuration = Duration.ofNanos(benchmark.nanoTime());
        String testName = benchmark.testName() == null ? method.getName() : benchmark.testName();
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Cannot call _benchmarkStatic on a non-static method");
        }
        return benchmarkStaticMethod(method, dataToTest.stream(), maxDuration, benchmark.clockFrequency(),
                                            benchmark.idName(), benchmark.idIsMethod(), testName);
    }

    @BenchmarkSuite
    private static class DefaultSettings {
    }

}
