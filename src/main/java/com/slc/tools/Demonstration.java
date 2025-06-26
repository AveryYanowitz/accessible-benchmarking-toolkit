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

    private static List<Integer> getRandom(int size) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt());
        }
        return list;
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        List<Integer> l1 = getRandom(1000);
        List<Integer> l2 = getRandom(2000);
        List<Integer> l3 = getRandom(4000);
        List<Integer> l4 = getRandom(8000);
        Stream<List<Integer>> listStream = Stream.of(l1, l2, l3, l4);
        
        
        List<BenchmarkStats> results = Benchmarking.benchmarkConsumer(Demonstration::bubbleSort, listStream,
                                                Duration.ofMillis(1000), 10, "size", true)
                                                .toList();
        for (BenchmarkStats stat : results) {
            System.out.println(stat);
        }
    }
}
