package com.slc.tools.examples;

import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.runners.ClassRunner;

public class StaticMethods {
    public static void main(String[] args) throws Exception {
        Stream<List<Integer>> dataList = Sorters.getRandomIntStream(5, 16, "exponential");
        ClassRunner.runBenchmarks(Sorters.class, dataList);
    }
}
