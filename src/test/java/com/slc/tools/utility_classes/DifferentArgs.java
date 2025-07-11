package com.slc.tools.utility_classes;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.OutputType;

@BenchmarkSuite(outputTo = OutputType.RETURN)
public class DifferentArgs {
    @Benchmarkable(nanoTime = 10_000_000)
    public void intArg(int x) {  }

    @Benchmarkable(nanoTime = 10_000_000)
    public void booleanArg(boolean x) {  }

    @Benchmarkable(nanoTime = 10_000_000)
    public void objectArg(Object x) {  }
}
