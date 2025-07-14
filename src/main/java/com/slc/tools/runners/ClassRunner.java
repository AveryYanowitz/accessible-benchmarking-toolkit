package com.slc.tools.runners;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Runs all of the `@Benchmarkable` methods written in a given class, using a different List to test each method.
     * If there are more methods than Lists, the program will use the last-provided list for all the extra methods.
     * For example, if you provide a class with three that each take one integer as input, and only two Lists of integers,
     * the first method (alphabetically) will be run with the first list, and the second method with the second list. The third method
     * will then <b> also </b> be run with the third list.
     * 
     * <p> If more inputs are provided than Benchmarkable methods in the class, a warning will be printed to System.out
     * and the program will simply ignore the extra methods.
     * 
     * 
     * @param <C> The class that clazz represents (i.e. the class containing `@Benchmarkable` methods)
     * @param <T> The type of data the Benchmarkable methods take as input
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param inputs One stream of data per Benchmarkable method in clazz, matching the alphabetical order of the methods in clazz.
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When trying to output to JSON file, but the location is invalid or the file cannot be edited
     */
    public static <C> List<BenchmarkStats> runBenchmarks(Class<C> clazz, List<?>... inputs) 
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
        
        List<Method> methods = _getBenchmarkMethods(clazz);
        if (methods.size() < inputs.length) {
            System.out.println("NOTE: Too many inputs ("+inputs.length+") provided for "
                                +"number of methods ("+methods.size()+", skipping extras)");
        }
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            Stream<?> dataToTest = _getDataStreamAtIndex(inputs, i);
            Stream<BenchmarkStats> results;
            if (whenToInit == Frequency.PER_METHOD) {
                target = createNewInstance(clazz);
            }

            try {
                MethodRunner<C> methodRunner = new MethodRunner<C>(method, target, dataToTest);
                results = methodRunner.benchmark();
            } catch (Exception e) {
                printSkipMessage(method, e);
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
     * @param clazz The class containing the Benchmarkable methods you want to run
     * @param dataToTest A list of data to run the benchmark methods on
     * @return The results of methods with OutputType.RETURN; may be empty
     * @throws IOException When the given JSON file location is invalid, or the file cannot be written to
     */
    public static <C> List<BenchmarkStats> runBenchmarks(Class<C> clazz, Stream<?>... inputs) 
                                    throws IOException {
        return runBenchmarks(clazz, Arrays.asList(inputs));
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
        annotatedMethods.sort((method1, method2) -> method1.getName().compareTo(method2.getName()));
        return annotatedMethods;
    }

    /**
     * Return the nth element of inputs as a stream (via the <code>stream()</code> method), with error handling.
     * @param inputs All the inputs to search
     * @param n The index to look at
     * @return inputs[n] as a stream, or if n >= inputs.length, the final element of inputs as a stream, or null if that specified element is null
     */
    private static Stream<?> _getDataStreamAtIndex(List<?>[] inputs, int n) {
        List<?> list;
        if (n < inputs.length) {
            list = inputs[n];
        } else {
            list = inputs[inputs.length - 1];
        }

        if (list != null) {
            return list.stream();
        }
        return null;
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

    static void printSkipMessage(Method method, Exception e) {
        StringBuilder sb = new StringBuilder("Skipping method '");
        sb.append(method.getName());
        sb.append("()' because of fatal ");
        sb.append(e.getClass().getSimpleName());
        sb.append(": ");
        sb.append(e.getMessage());
        System.out.println(sb.toString());
        e.printStackTrace();
    }

    /** Just used to store the default BenchmarkSuite annotation for reference */
    @BenchmarkSuite
    private static class DefaultSettings {    }

}
