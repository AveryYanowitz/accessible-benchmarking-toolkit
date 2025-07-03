package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.slc.tools.annotations.Benchmark;
import com.slc.tools.annotations.Runner;

public class AnnotationTests {

    private static class BenchmarkHolder {
        @Benchmark
        public void isABenchmark() { }
        
        @SuppressWarnings("unused")
        public void isNotABenchmark() { }
    }

    @Test
    public void benchmarkFetchTest() throws NoSuchMethodException {
        BenchmarkHolder benchmarkHolder = new BenchmarkHolder();
        Method annotated = benchmarkHolder.getClass().getDeclaredMethod("isABenchmark");
        Method notAnnotated = benchmarkHolder.getClass().getDeclaredMethod("isNotABenchmark");

        List<Method> shouldOnlyBeBenchmarks = Runner.getBenchmarks(benchmarkHolder.getClass());
        assertTrue(shouldOnlyBeBenchmarks.contains(annotated));
        assertFalse(shouldOnlyBeBenchmarks.contains(notAnnotated));
    }

}
