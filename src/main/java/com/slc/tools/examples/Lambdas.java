package com.slc.tools.examples;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.runners.LambdaRunner;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.Jsonifier;

public class Lambdas {
    public static void main(String[] args) throws Exception {
        Stream<List<Integer>> listStream = Sorters.getRandomIntStream(1000, 10, "linear");
        
        Stream<BenchmarkStats> results1 = LambdaRunner.benchmarkConsumable(Sorters::bubbleSort, listStream,
                Duration.ofMillis(1000), 10,
                "size", true, "bubbleSort");

        Stream<List<Integer>> listStream2 = Sorters.getRandomIntStream(1000, 10, "linear");

        Stream<BenchmarkStats> results2 = LambdaRunner.benchmarkConsumable(Sorters::insertionSort, listStream2,
                Duration.ofMillis(100), 10,
                "size", true, "insertionSort");

        Jsonifier jsonifier = new Jsonifier(results1, results2);
        jsonifier.jsonify();
    }
}
