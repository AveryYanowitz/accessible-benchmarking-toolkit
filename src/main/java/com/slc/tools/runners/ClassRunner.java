package com.slc.tools.runners;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.FormatUtils;
import com.slc.tools.util.Jsonifier;

public class ClassRunner {
    /**
     * Runs all of the `@Benchmarkable` methods written in a given class and overrides the class's output type.
     * @param <C> The class that clazz represents (i.e. the class containing `@Benchmarkable` methods)
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest A stream of data to run the benchmark methods on
     * @param outputOverride Overrides a specified OutputType in clazz
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When the given JSON file location is invalid, or the file cannot be written to
     */
    public static <C, T> List<BenchmarkStats> runBenchmarks(Class<C> clazz, Stream<T> dataToTest) 
                                    throws IOException {
        Jsonifier jsonifier = Jsonifier.getJsonifier(clazz);
        BenchmarkSuite classAnno = clazz.getAnnotation(BenchmarkSuite.class);
        if (classAnno == null) {
            classAnno = DefaultSettings.class.getAnnotation(BenchmarkSuite.class);
        }

        List<BenchmarkStats> resultsList = new ArrayList<>();
        Frequency whenToInit = classAnno.whenToInstantiate();
        C target = null;
        OutputType outputTo = classAnno.outputTo();

        if (whenToInit == Frequency.ON_INIT) {
            target = createNewInstance(clazz);
        }
        
        for (Method method : _getBenchmarkMethods(clazz)) {
            Stream<BenchmarkStats> results;
            try {                
                switch (whenToInit) {
                    case ON_INIT:
                        results = MethodRunner.benchmarkMethod(method, target, dataToTest);
                        break;
                    case PER_METHOD:
                        target = createNewInstance(clazz);
                        results = MethodRunner.benchmarkMethod(method, target, dataToTest);
                        break;
                    default: // NEVER or PER_SIZE_VALUE
                        results = MethodRunner.benchmarkMethod(method, dataToTest);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Skipping method " + method.getName() + "because: ");
                System.out.println(e.getMessage());
                continue;
            }

            switch (outputTo) {
                case PRINT:
                    results.forEach((result) -> {
                        System.out.println(result);
                    });
                    break;
                case JSON:
                    jsonifier.addToJson(results);
                    break;
                case RETURN:
                    results.forEach(resultsList::add);
                    break;
            }
        }
        if (jsonifier.size() > 0) {
            jsonifier.jsonify();
        }
        return resultsList;
    }

    /**
     * Runs all of the `@Benchmarkable` methods written in a given class and overrides the class's output type.
     * @param <C> The class that clazz represents (i.e. the class containing `@Benchmarkable` methods)
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @param outputOverride Overrides a specified OutputType in clazz
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When the given JSON file location is invalid, or the file cannot be written to
     */
    public static <C, T> List<BenchmarkStats> runBenchmarks(Class<C> clazz, List<T> dataToTest) 
                                    throws IOException {
        return runBenchmarks(clazz, dataToTest.stream());
    }

    /**
     * Runs all of the `@Benchmarkable` methods written in a given class and overrides the class's output type.
     * @param <C> The class that clazz represents (i.e. the class containing `@Benchmarkable` methods)
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest An array of data to run the benchmark methods on
     * @param outputOverride Overrides a specified OutputType in clazz
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When the given JSON file location is invalid, or the file cannot be written to
     */
    public static <C, T> List<BenchmarkStats> runBenchmarks(Class<C> clazz, T[] dataToTest) throws IOException {
        Stream<T> asStream = FormatUtils.toStream(dataToTest);
        return runBenchmarks(clazz, asStream);
    }

    /**
     * Fetches all the non-synthetic methods in clazz annotated with Benchmarkable
     * @param clazz The class containing Benchmarkable methods, 
     * which may or may not be marked with BenchmarkSuite
     * @return A List of the Benchmarkable methods, which may be empty
     */
    private static List<Method> _getBenchmarkMethods(Class<?> clazz) {
        Method[] classMethods = clazz.getDeclaredMethods();
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(Benchmarkable.class)
                && !method.isSynthetic()) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    /**
     * Create a new instance of clazz using its no-args constructor, or return null if none found
     * @param <T> The type you want to create a new instance of
     * @param clazz A Class object representing T
     * @return A new instance of type T, or null if no zero-args constructor was found
     */
    static <T> T createNewInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            // Thrown when wrong arguments are passed to constructor, or no such constructor exists;
            // in this case, it means no zero-args constructor is present and the benchmark was invalid
            System.out.print("WARNING: Unable to instantiate object of type "+clazz.getSimpleName());
            System.out.print(". Make sure class has a visible no-args constructor, is marked with ");
            System.out.println("@BenchmarkSuite, and specifies a value for whenToInstantiate other than NEVER");
            e.printStackTrace();
            return null;
        }
    }

    /** Just used to store the default BenchmarkSuite annotation for reference */
    @BenchmarkSuite
    private static class DefaultSettings {    }

}
