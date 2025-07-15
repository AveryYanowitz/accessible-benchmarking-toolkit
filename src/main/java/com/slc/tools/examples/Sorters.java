package com.slc.tools.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;

@BenchmarkSuite
public class Sorters {

    @Benchmarkable(testName = "Bubble Sort")
    public static boolean bubbleSort(List<Integer> a) {
        for (int i = 0; i < a.size(); i++) {
            for (int j = i; j < a.size(); j++) {
                if (a.get(i) > a.get(j)) {
                    _swap(a, i, j);
                }
            }
        }
        return true;
    }
    
    @Benchmarkable(testName = "Insertion Sort")
    public static void insertionSort(List<Integer> arrToSort) {
        for (int i = 1; i < arrToSort.size(); i++) {
            _insertIntoSorted(arrToSort, i, arrToSort.get(i));
        }
    }

    private static void _swap(List<Integer> a, int i1, int i2) {
        int placeholder = a.get(i1);
        a.set(i1, a.get(i2));
        a.set(i2, placeholder);
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

    public enum TestGrowth {
        LINEAR,
        EXPONENTIAL
    }

    /** Generate Streams for testing automatically
     * @param minSize minimum size of the lists to generate, list size increases linearly relative to listNUmber
     * @param listNumber number of lists to generate
     * @param testGrowth specify growth rate of "size" in tests, either TestGrowth.LINEAR or TestGrowth.EXPONENTIAL
     * @return a stream of 
     */
    public static Stream<List<Integer>> getRandomIntStream(int minSize, int listNumber, TestGrowth testGrowth) {
        Stream.Builder<List<Integer>> sb = Stream.builder();
        switch (testGrowth) {
            case LINEAR:
                for (int i = 0; i < listNumber; i++) {
                    sb.add(getRandomIntList((1 + i) * minSize));
                }                
                break;
            case EXPONENTIAL:
                for (int i = 0; i < listNumber; i++) {
                    sb.add(getRandomIntList((1 << i) * minSize));
                }
                break;
        }
        return sb.build();
    }

    public static List<Integer> getRandomIntList(int size) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt());
        }
        return list;
    }

    public static List<Integer> getRandomIntList(int size, int min, int max) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt(min, max));
        }
        return list;
    }

    public static List<Boolean> getRandomBoolList(int size) {
        List<Boolean> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(random.nextBoolean());
        }
        return list;
    }

    public static List<Object> getObjList(int size) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Object());
        }
        return list;
    }

}
