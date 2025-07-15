package com.slc.tools.utility_classes;

import java.util.ArrayDeque;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;


@BenchmarkSuite(whenToInstantiate = Frequency.ON_INIT, outputTo = OutputType.RETURN)
public class ArrDequeWrapper extends ArrayDeque<Integer> {
    static int numberOfInstancesMade = 0;
    
    public ArrDequeWrapper() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000)
    @Override
    public boolean add(Integer element) {
        return super.add(element);
    }

    @Benchmarkable(nanoTime = 10_000_000)
    public void otherMethod(Integer x) {
        
    }

    public static int getInstances() {
        return numberOfInstancesMade;
    }
}
