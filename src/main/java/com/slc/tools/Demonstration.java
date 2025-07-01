package com.slc.tools;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Demonstration {
    public static void bubbleSort(List<Integer> a) {
        for (int i = 0; i < a.size(); i++) {
            for (int j = i; j < a.size(); j++) {
                if (a.get(i) > a.get(j)) {
                    _swap(a, i, j);
                }
            }
        }
    }

    private static void _swap(List<Integer> a, int i1, int i2) {
        int placeholder = a.get(i1);
        a.set(i1, a.get(i2));
        a.set(i2, placeholder);
    }

    public static void insertionSort(List<Integer> arrToSort) {
        for (int i = 1; i < arrToSort.size(); i++) {
            _insertIntoSorted(arrToSort, i, arrToSort.get(i));
        }
    }

    // Assume arr is sorted through the highestSortedIndex; returns the index of
    // insertion
    private static void _insertIntoSorted(List<Integer> arr, int highestSortedIndex, int n) {
        for (int i = highestSortedIndex; i > 0; i--) {
            if (n < arr.get(i - 1)) {
                arr.set(i, arr.get(i - 1));
            } else {
                arr.set(i, n);
                return;
            }
        }
        arr.set(0, n);
    }

    private static List<Integer> _getRandom(int size) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt());
        }
        return list;
    }

    /** Generate Streams for testing automatically
     * @param minSize minimum size of the lists to generate, list size increases linearly relative to listNUmber
     * @param listNumber number of lists to generate
     * @param testGrowth specify growth rate of "size" in tests, either "linear" or "exponential"
     * @return a stream of 
     */
    private static Stream<List<Integer>> _getIntTestStream(int minSize, int listNumber, String testGrowth) {
        Stream.Builder<List<Integer>> sb = Stream.builder();
        testGrowth = testGrowth.toLowerCase();
        if (testGrowth.equals("linear")) {
            for (int i = 0; i < listNumber; i++) {sb.add(_getRandom((1 + i) * minSize));}
        } else if (testGrowth.equals("exponential")) {
            for (int i = 0; i < listNumber; i++) {sb.add(_getRandom((1 << i) * minSize));}
        } else {
            throw new RuntimeException("invalid growth rate: " + testGrowth);
        }
        return sb.build();
    }

    public static void main(String[] args) throws Exception {
        Stream<List<Integer>> listStream = _getIntTestStream(1000, 10, "linear");

        List<BenchmarkStats> results1 = Benchmarking.benchmarkConsumer(Demonstration::bubbleSort, listStream,
                Duration.ofMillis(1000), 10,
                "size", true, "bubbleSort")
                .toList();

        Stream<List<Integer>> listStream2 = _getIntTestStream(1000, 10, "linear");

        List<BenchmarkStats> results2 = Benchmarking.benchmarkConsumer(Demonstration::insertionSort, listStream2,
                Duration.ofMillis(1000), 10,
                "size", true, "insertionSort")
                .toList();

        BenchmarkStats.printStats(results1, results2);
        Jsonifier.jsonify(results1, results2);
    }
}
