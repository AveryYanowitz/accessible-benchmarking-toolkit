package com.slc.tools;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.OutputType;

@BenchmarkSuite(outputTo = OutputType.RETURN, saveLocation = "src/test/output")
public class BenchmarkHolder {
    static int numberOfInstancesMade = 0;
    public BenchmarkHolder() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
    public static void emptyBenchmark(int x) { }

    @Benchmarkable(nanoTime = 10_000_000, idName = "intValue", idIsMethod = true)
    public static int realBenchmark(int x) {
        return x*x;
    }

    @Benchmarkable
    public void incorrectBenchmark() { }

    @Benchmarkable
    private void privateBenchmark() { } // shouldn't be tested at all
    
    public void notABenchmark() { }

}