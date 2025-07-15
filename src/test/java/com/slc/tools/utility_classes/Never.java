package com.slc.tools.utility_classes;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;

@BenchmarkSuite(outputTo = OutputType.RETURN, whenToInstantiate = Frequency.NEVER)
public class Never {
    static int numberOfInstancesMade = 0;
    public Never() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
    public static void emptyBenchmark(int x) { }

    @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
    public static int notEmptyBenchmark(int x) {
        return x*x;
    }

    public static int getInstances() {
        return numberOfInstancesMade;
    }

}