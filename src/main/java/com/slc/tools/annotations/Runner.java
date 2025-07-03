package com.slc.tools.annotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Runner {
    public static <T> void runBenchmarks(Class<T> classWithBenchmarks, Stream<T> dataToTest) {
        List<Method> methodsToTest = getBenchmarks(classWithBenchmarks);
        
    }

    public static <T> void runBenchmarks(T instance) {
        // List<Method> methodsToBenchmark = _getBenchmarks(clazz);

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
