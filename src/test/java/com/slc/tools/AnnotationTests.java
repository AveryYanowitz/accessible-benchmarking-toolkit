package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.annotations.Runner;
import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.examples.ExampleClass;

public class AnnotationTests {

    @BenchmarkSuite(outputTo = OutputType.RETURN, saveLocation = "src/test/output")
    public static class BenchmarkHolder {
        @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
        public static void emptyBenchmark(int x) { }

        @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
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
    public void returnTest() {
        Class<BenchmarkHolder> clazz = BenchmarkHolder.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        List<BenchmarkStats> results;

        try {
            results = Runner.runBenchmarks(clazz, randomInts);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        assertEquals(8, results.size());
        
        for (BenchmarkStats result : results) {
            assertNotNull(result);
            assertTrue(result.isComplete());
        }
    }

    @Test
    public void privateTest() {
        Class<BenchmarkHolder.InnerClass> privateClazz = BenchmarkHolder.InnerClass.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        List<BenchmarkStats> results;
        try {
            results = Runner.runBenchmarks(privateClazz, randomInts);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        assertEquals(0, results.size()); // since InnerClass is private, this shouldn't have run anything
    }

    @Test
    public void jsonSaveTest() {
        Class<BenchmarkHolder> clazz = BenchmarkHolder.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        List<BenchmarkStats> results;
        
        String beforeText;
        String afterText;
        try {
            beforeText = _getJsonText();
            results = Runner.runBenchmarks(clazz, randomInts, OutputType.JSON);
            afterText = _getJsonText();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        assertEquals(0, results.size());
        assertNotEquals(beforeText, afterText);
    }

    private static String _getJsonText() {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File("src/test/output/results.json"));
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            return "";
        }
    }

}
