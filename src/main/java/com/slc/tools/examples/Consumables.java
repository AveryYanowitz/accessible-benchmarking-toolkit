package com.slc.tools.examples;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.benchmarks.BenchmarkStats;
import com.slc.tools.benchmarks.BenchmarkingFuncs;
import com.slc.tools.benchmarks.Jsonifier;

public class Consumables {
    public static void main(String[] args) throws Exception {
        Stream<List<Integer>> listStream = ExampleClass.getRandomIntStream(1000, 10, "linear");
        
        Stream<BenchmarkStats> results1 = BenchmarkingFuncs.benchmarkConsumable(ExampleClass::bubbleSort, listStream,
                Duration.ofMillis(1000), 10,
                "size", true, "bubbleSort");

        Stream<List<Integer>> listStream2 = ExampleClass.getRandomIntStream(1000, 10, "linear");

        Stream<BenchmarkStats> results2 = BenchmarkingFuncs.benchmarkConsumable(ExampleClass::insertionSort, listStream2,
                Duration.ofMillis(100), 10,
                "size", true, "insertionSort");

        Jsonifier jsonifier = new Jsonifier(results1, results2);
        jsonifier.jsonify();
    }
}
