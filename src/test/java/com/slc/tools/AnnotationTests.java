package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.slc.tools.annotations.OutputType;
import com.slc.tools.annotations.Runner;
import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.examples.ExampleClass;

public class AnnotationTests {

    @Test
    public void frequencyNever() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<BenchmarkHolder> clazz = BenchmarkHolder.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        Runner.runBenchmarks(clazz, randomInts);
        assertEquals(0, BenchmarkHolder.numberOfInstancesMade); // all methods are static, so no instances should be created
    }

    @Test
    public void frequencyInit() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<ArrDequeWrapper> clazz = ArrDequeWrapper.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        Runner.runBenchmarks(clazz, randomInts);
        assertEquals(1, ArrDequeWrapper.numberOfInstancesMade); // should only make one instance upon starting the tests
    }

    @Test
    public void frequencyMethod() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<ArrListWrapper> clazz = ArrListWrapper.class;
        List<Integer> randomInts = ExampleClass.getRandomIntList(4);
        Runner.runBenchmarks(clazz, randomInts);
        assertEquals(2, ArrListWrapper.numberOfInstancesMade); // all methods are static, so no instances should be created
        
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
