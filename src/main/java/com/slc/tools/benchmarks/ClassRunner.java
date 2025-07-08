package com.slc.tools.benchmarks;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.Jsonifier;

public class ClassRunner {
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
        if (classAnno == null) {
            return runBenchmarks(clazz, dataToTest, OutputType.JSON);
        } else {
            return runBenchmarks(clazz, dataToTest, classAnno.outputTo());
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
        Jsonifier jsonifier = Jsonifier.getJsonifier(clazz);

        List<BenchmarkStats> resultsList = new ArrayList<>();
        Frequency whenToInit = clazz.getAnnotation(BenchmarkSuite.class).whenToInstantiate();
        C target = null; // make sure it's init'd
        if (whenToInit == Frequency.ON_INIT) {
            target = InstanceMethods.createNewInstance(clazz);
        }
        
        for (Method method : _getBenchmarkMethods(clazz)) {
            System.out.println(clazz.getSimpleName() + " " + method.getName());
            Stream<BenchmarkStats> results;
            if (Modifier.isStatic(method.getModifiers())) {
                results = StaticMethods.benchmarkStaticMethod(method, dataToTest);
            } else {
                if (whenToInit == Frequency.NEVER) {
                    continue; // can't test non-static methods if we're not allowd to make an instance
                }
                // if (whenToInit == Frequency.PER_METHOD) {
                //     target = _createNewInstance(clazz);
                // }
                results = InstanceMethods.benchmarkInstanceMethod(method, clazz, target, dataToTest);
            }

            if (outputTo == OutputType.PRINT) {
                results.forEach((result) -> {
                    System.out.println(result);
                });
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

    private static <T> List<Method> _getBenchmarkMethods(Class<T> clazz) throws InstantiationException {
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


    /** Just used to store the default \@BenchmarkSuite annotation for reference */
    @BenchmarkSuite
    private static class DefaultSettings {    }

}
