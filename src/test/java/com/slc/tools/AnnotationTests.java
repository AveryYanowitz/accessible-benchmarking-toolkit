package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.slc.tools.annotations.Benchmark;
import com.slc.tools.annotations.Runner;

public class AnnotationTests {

    private static class BenchmarkHolder {
        @Benchmark
        public static void correctBenchmark() { }

        @Benchmark
        public void incorrectBenchmark() { }
        
        @SuppressWarnings("unused")
        public void notABenchmark() { }
    }

    @Test
    public void annotatedFetchTest() throws NoSuchMethodException {
        BenchmarkHolder benchmarkHolder = new BenchmarkHolder();
        Method correct = benchmarkHolder.getClass().getDeclaredMethod("correctBenchmark");
        Method incorrect = benchmarkHolder.getClass().getDeclaredMethod("incorrectBenchmark");
        Method notAnnotated = benchmarkHolder.getClass().getDeclaredMethod("notABenchmark");

        List<Method> shouldOnlyBeBenchmarks = Runner.getBenchmarks(benchmarkHolder.getClass());
        assertTrue(shouldOnlyBeBenchmarks.contains(correct));
        assertFalse(shouldOnlyBeBenchmarks.contains(incorrect));
        assertFalse(shouldOnlyBeBenchmarks.contains(notAnnotated));
    }

    @Test
    public void runnerTest() {

    }

}
