package com.slc.tools;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Demonstration {
    public static void bubbleSort(int[] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = i; j < a.length; j++) {
                if (a[i] > a[j]) {
                    _swap(a, i, j);
                }
            }
        }
    }

    private static void _swap(int[] a, int i1, int i2) {
        int placeholder = a[i1];
        a[i1] = a[i2];
        a[i2] = placeholder;
    }

    public static void main(String[] args) {
        Random r = new Random();
        int[] a1 = r.ints(1000).toArray();
        int[] a2 = r.ints(2000).toArray();
        int[] a3 = r.ints(4000).toArray();
        int[] a4 = r.ints(8000).toArray();
        Stream<int[]> arrsToTest = Stream.of(a1, a2, a3, a4);
        
        List<BenchmarkStats> results = Benchmarking.testStream(arrsToTest, Demonstration::bubbleSort,
                                                Duration.ofMillis(1000), 10)
                                                .toList();
        for (BenchmarkStats stat : results) {
            System.out.println(stat);
        }
    }
}
