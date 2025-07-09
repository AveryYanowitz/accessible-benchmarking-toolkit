package com.slc.tools.runners;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
                                                        throws IOException {
        Jsonifier jsonifier = Jsonifier.getJsonifier(clazz);

        List<BenchmarkStats> resultsList = new ArrayList<>();
        Frequency whenToInit = clazz.getAnnotation(BenchmarkSuite.class).whenToInstantiate(); //TODO: possible NPE
        C target = null; // make sure it's init'd
        boolean instanceMethodsAllowed = true;

        if (whenToInit == Frequency.ON_INIT) {
            target = _createNewInstance(clazz);
            instanceMethodsAllowed = (target == null); // returns null if no constructor visible
        }
        
        for (Method method : _getBenchmarkMethods(clazz)) {
            Stream<BenchmarkStats> results;
            if (Modifier.isStatic(method.getModifiers())) {
                results = MethodRunner.benchmarkStaticMethod(method, dataToTest);
            } else if (instanceMethodsAllowed) {
                if (whenToInit == Frequency.NEVER) {
                    _warnFrequencyNever(clazz);
                    instanceMethodsAllowed = false;
                    continue;
                }
                if (whenToInit == Frequency.PER_METHOD) {
                    target = _createNewInstance(clazz);
                    if (target == null) { // returns null if no constructor visible
                        instanceMethodsAllowed = false;
                        continue;
                    }
                }
                results = MethodRunner.benchmarkInstanceMethod(method, target, dataToTest);
            } else {
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

    private static <T> List<Method> _getBenchmarkMethods(Class<T> clazz) {
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

    private static <T> T _createNewInstance(Class<T> clazz) {
        String className = clazz.getSimpleName();
        try {
            return clazz.getConstructor().newInstance();
        } catch (IllegalArgumentException | NoSuchMethodException e) {
            // Thrown when wrong arguments are passed to constructor, or no such constructor exists;
            // in this case, it means no zero-args constructor is present and the benchmark was invalid
            System.out.print("WARNING: Unable to test non-static methods in class "+clazz.getSimpleName());
            System.out.print("because class is either missing @BenchmarkSuite annotation,");
            System.out.println("does not specify value for whenToInstantiate, or specifies Frequency.NEVER");
            return null;
        } catch (IllegalAccessException | SecurityException | InstantiationException | InvocationTargetException e) {
            // Other errors don't reflect a problem with the benchmarking annotations,
            // so we won't throw anything, just warn the user
            System.out.print("Warning: Unable to instantiate object of type ");
            System.out.print(className);
            System.out.println("; skipping benchmark");
            return null;
        }
    }

    private static void _warnFrequencyNever(Class<?> clazz) {
        /* There are three possible reasons this function was called:
         * 
         * 1. clazz is missing the @BenchmarkSuite annotation
         * 2. clazz's @BenchmarkSuite annotation does not specify whenToInstantiate
         * 3. clazz's @BenchmarkSuite annotation specifies Frequency.NEVER
         * 
         * The 2nd and 3rd issues are difficult to tell apart at runtime, 
         * so this program groups them together for the purposes of warnings.
         */
        StringBuilder sb = new StringBuilder("WARNING: Unable to test non-static methods in class ");
        sb.append(clazz.getSimpleName());
        sb.append(" because ");
        if (clazz.isAnnotationPresent(BenchmarkSuite.class)) {
            sb.append("class is missing @BenchmarkSuite annotation");
        } else {
            sb.append("class does not specify value for whenToInstantiate, or specifies Frequency.NEVER");
        }
        System.out.println(sb.toString());
    }

    /** Just used to store the default \@BenchmarkSuite annotation for reference */
    @BenchmarkSuite
    private static class DefaultSettings {    }

}
