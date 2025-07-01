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

    // Assume arr is sorted through the highestSortedIndex; returns the index of insertion
    private static void _insertIntoSorted(List<Integer> arr, int highestSortedIndex, int n) {
        for (int i = highestSortedIndex; i > 0; i--) {
            if (n < arr.get(i - 1)) {
                arr.set(i, arr.get(i-1));
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

    public static void main(String[] args) throws Exception {
        List<Integer> l1 = _getRandom(1000);
        List<Integer> l2 = _getRandom(2000);
        List<Integer> l3 = _getRandom(4000);
        List<Integer> l4 = _getRandom(8000);
        Stream<List<Integer>> listStream = Stream.of(l1, l2, l3, l4);
        
        List<BenchmarkStats> results1 = Benchmarking.benchmarkConsumer(Demonstration::bubbleSort, listStream,
                                                Duration.ofMillis(1000), 10, 
                                                "size", true, "bubbleSort")
                                                .toList();
        
        List<Integer> l5 = _getRandom(1000);
        List<Integer> l6 = _getRandom(2000);
        List<Integer> l7 = _getRandom(4000);
        List<Integer> l8 = _getRandom(8000);
        Stream<List<Integer>> listStream2 = Stream.of(l5, l6, l7, l8);
        
        List<BenchmarkStats> results2 = Benchmarking.benchmarkConsumer(Demonstration::insertionSort, listStream2,
                                                Duration.ofMillis(1000), 10, 
                                                "size", true, "insertionSort")
                                                .toList();
        
        BenchmarkStats.printStats(results1, results2);                                        
        Jsonifier.jsonify(results1, results2);
    }
}
