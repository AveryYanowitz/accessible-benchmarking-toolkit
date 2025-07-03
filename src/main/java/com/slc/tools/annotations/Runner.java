package com.slc.tools.annotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Runner {
    public static void runBenchmarks(Class<?> clazz) {
        List<Method> methodsToBenchmark = _getBenchmarks(clazz);
        
    }

    private static List<Method> _getBenchmarks(Class<?> clazz) {
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
