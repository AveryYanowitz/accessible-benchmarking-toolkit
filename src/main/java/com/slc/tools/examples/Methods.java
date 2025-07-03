package com.slc.tools.examples;

import java.util.ArrayList;
import java.util.List;

import com.slc.tools.annotations.Runner;

public class Methods {
    public static void main(String[] args) throws Exception {
        List<List<Integer>> dataList = new ArrayList<>();
        for (int i = 16; i <= 256; i*=2) {
            dataList.add(ExampleClass.getRandomIntList(i));
        }
        Runner.runBenchmarks(ExampleClass.class, dataList);
    }
}
