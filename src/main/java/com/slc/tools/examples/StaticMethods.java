package com.slc.tools.examples;

import java.util.ArrayList;
import java.util.List;

import com.groupid.Generators.LCGenerator;
import com.slc.tools.runners.ClassRunner;

public class StaticMethods {
    public static void main(String[] args) throws Exception {
        List<List<Integer>> dataList = new ArrayList<>();
        for (int i = 16; i <= 256; i*=2) {
            LCGenerator lcg = new LCGenerator.LcgBuilder()
                                    .limit(i)
                                    .build();
            dataList.add(lcg.toList());
        }
        ClassRunner.runBenchmarks(Sorters.class, dataList);
    }
}
