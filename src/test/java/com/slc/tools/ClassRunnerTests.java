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

import com.slc.tools.examples.Sorters;
import com.slc.tools.runners.ClassRunner;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.utility_classes.ArrDequeWrapper;
import com.slc.tools.utility_classes.ArrListWrapper;
import com.slc.tools.utility_classes.EachSize;
import com.slc.tools.utility_classes.Never;
import com.slc.tools.utility_classes.JsonBenchmarks;


public class ClassRunnerTests {

    @Test
    public void frequencyNever() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<Never> clazz = Never.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        assertEquals(0, Never.getInstances()); // all methods are static, so no instances should be created
    }

    @Test
    public void frequencyInit() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<ArrDequeWrapper> clazz = ArrDequeWrapper.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        // Expected instances: 1 from ON_INIT + 1 per method from _isValidMethod
        assertEquals(2, ArrDequeWrapper.getInstances()); // should only make one instance, upon starting the tests
    }

    @Test
    public void frequencyMethod() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<ArrListWrapper> clazz = ArrListWrapper.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        // Expected instances: 1 per method from PER_METHOD + 1 per method from _isValidMethod
        assertEquals(4, ArrListWrapper.getInstances()); // should make a new instance for each @Benchmarkable method
    }

    @Test
    public void frequencySizeValue() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        Class<EachSize> clazz = EachSize.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        assertEquals(randomInts.size(), ArrListWrapper.getInstances()); // should make a new instance for each test case
    }

    @Test
    public void returnTest() throws IOException {
        Class<Never> clazz = Never.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        List<BenchmarkStats> results;

        results = ClassRunner.runBenchmarks(clazz, randomInts);
        assertEquals(8, results.size());
        
        for (BenchmarkStats result : results) {
            assertNotNull(result);
            assertTrue(result.isComplete());
        }
    }

    @Test
    public void jsonSaveTest() {
        Class<JsonBenchmarks> clazz = JsonBenchmarks.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        List<BenchmarkStats> results;
        
        String beforeText;
        String afterText;
        try {
            beforeText = _getJsonText();
            results = ClassRunner.runBenchmarks(clazz, randomInts);
            afterText = _getJsonText();
        } catch (Exception e) {
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
