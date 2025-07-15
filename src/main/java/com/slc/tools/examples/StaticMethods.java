package com.slc.tools.examples;

import java.util.List;
import java.util.stream.Stream;

import com.slc.tools.runners.ClassRunner;

public class StaticMethods {
    public static void main(String[] args) throws Exception {
        Stream<List<Integer>> dataStream = Sorters.getRandomIntStream(5, 6, Sorters.TestGrowth.EXPONENTIAL);
        ClassRunner.runBenchmarks(Sorters.class, dataStream);
    }
}
