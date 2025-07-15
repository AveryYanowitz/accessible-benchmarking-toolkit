package com.slc.tools.utility_classes;


import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;

@BenchmarkSuite(whenToInstantiate = Frequency.PER_SIZE_VALUE)
public class EachSize {
    static int numberOfInstancesMade;
    public EachSize() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
    public void baz(int x) {    }
}
