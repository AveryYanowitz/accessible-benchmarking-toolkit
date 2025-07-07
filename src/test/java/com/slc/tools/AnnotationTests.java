package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.annotations.Runner;
import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.examples.ExampleClass;

public class AnnotationTests {

    public static class BenchmarkHolder {
        @Benchmarkable(nanoTime = 1_000_000, outputTo = OutputType.RETURN,
        idName = "intValue", idIsMethod = true)
        public static void emptyBenchmark(int x) { }

        @Benchmarkable(nanoTime = 1_000_000, outputTo = OutputType.RETURN,
        idName = "intValue", idIsMethod = true)
        public static int realBenchmark(int x) {
            return x*x;
        }

        @Benchmarkable
        public void incorrectBenchmark() { }

        @Benchmarkable
        private void privateBenchmark() { } // shouldn't be tested at all
        
        public void notABenchmark() { }

        private static class InnerClass {
            @Benchmarkable
            public static void privateInner(int x) { } // also shouldn't be tested
        }

    }

    @Test
    public void annotatedFetchTest() throws NoSuchMethodException {
        Method correct = BenchmarkHolder.class.getDeclaredMethod("emptyBenchmark", int.class);
        Method incorrect = BenchmarkHolder.class.getDeclaredMethod("incorrectBenchmark");
        Method notAnnotated = BenchmarkHolder.class.getDeclaredMethod("notABenchmark");

        List<Method> shouldOnlyBeBenchmarks = Runner.getBenchmarks(BenchmarkHolder.class);
        assertTrue(shouldOnlyBeBenchmarks.contains(correct));
        assertFalse(shouldOnlyBeBenchmarks.contains(incorrect));
        assertFalse(shouldOnlyBeBenchmarks.contains(notAnnotated));
    }



    @Test
    public void runnerTest() {
        Class<BenchmarkHolder> clazz = BenchmarkHolder.class;
        Class<BenchmarkHolder.InnerClass> privateClazz = BenchmarkHolder.InnerClass.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        List<BenchmarkStats> fullResults;
        List<BenchmarkStats> emptyResults;

        try {
            fullResults = Runner.runBenchmarks(clazz, randomInts);
            emptyResults = Runner.runBenchmarks(privateClazz, randomInts);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        assertEquals(8, fullResults.size());
        assertEquals(0, emptyResults.size());
        for (BenchmarkStats result : fullResults) {
            assertNotNull(result);
            assertTrue(result.isComplete());
        }
    }
}
