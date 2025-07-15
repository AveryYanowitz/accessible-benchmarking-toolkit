package com.slc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import com.slc.tools.examples.Sorters;
import com.slc.tools.runners.ClassRunner;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.utility_classes.ArrDequeWrapper;
import com.slc.tools.utility_classes.ArrListWrapper;
import com.slc.tools.utility_classes.DifferentArgs;
import com.slc.tools.utility_classes.EachSize;
import com.slc.tools.utility_classes.Never;
import com.slc.tools.utility_classes.JsonBenchmarks;


public class ClassRunnerTests {

    @Test
    public void frequencyNever() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<Never> clazz = Never.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        assertEquals(0, Never.getInstances()); // all methods are static, so no instances should be created
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);
    }

    @Test
    public void frequencyInit() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<ArrDequeWrapper> clazz = ArrDequeWrapper.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);

        // Expected instances: 1 from ON_INIT + 1 per method from _isValidMethod
        assertEquals(3, ArrDequeWrapper.getInstances());
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);
    }

    @Test
    public void frequencyMethod() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<ArrListWrapper> clazz = ArrListWrapper.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);

        // Expected instances: 1 per method from PER_METHOD + 1 per method from _isValidMethod
        assertEquals(4, ArrListWrapper.getInstances());
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);
    }

    @Test
    public void frequencySizeValue() throws IllegalArgumentException, IOException, ReflectiveOperationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<EachSize> clazz = EachSize.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        ClassRunner.runBenchmarks(clazz, randomInts);
        assertEquals(randomInts.size(), ArrListWrapper.getInstances());
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);
    }

    @Test
    public void returnTest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<Never> clazz = Never.class;
        List<Integer> randomInts = Sorters.getRandomIntList(4);
        List<BenchmarkStats> results;

        results = ClassRunner.runBenchmarks(clazz, randomInts);
        // Expected size: 2 methods * 4 test cases = 8
        assertEquals(8, results.size());
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);
        
        for (BenchmarkStats result : results) {
            assertNotNull(result);
            assertTrue(result.isComplete(), out.toString());
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
            e.printStackTrace();
            return;
        }

        assertEquals(0, results.size());
        assertNotEquals(beforeText, afterText);
    }

    @Test
    public void differentArgsTest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Class<DifferentArgs> clazz = DifferentArgs.class;
        List<Integer> intArgs = Sorters.getRandomIntList(4);
        List<Boolean> boolArgs = Sorters.getRandomBoolList(4);
        List<Object> objArgs = Sorters.getObjList(4);
        
        List<BenchmarkStats> results = ClassRunner.runBenchmarks(clazz, boolArgs, intArgs, null, objArgs);
        
        String outTxt = out.toString();
        assertFalse(outTxt.contains("Skipping method"), outTxt);

        // Expected: 4 tries each of intArgs, boolArgs, and objArgs, plus 1 of noArgs
        assertEquals(13, results.size());
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
