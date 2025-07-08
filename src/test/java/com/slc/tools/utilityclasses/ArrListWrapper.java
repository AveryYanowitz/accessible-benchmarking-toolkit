package com.slc.tools.utilityclasses;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;

import edu.slc.cs.stack_queue_comparison.ArrayListQueue;

@BenchmarkSuite(whenToInstantiate = Frequency.PER_METHOD, outputTo = OutputType.RETURN)
public class ArrListWrapper extends ArrayListQueue<Integer> {
    static int numberOfInstancesMade = 0;

    public ArrListWrapper() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000)
    @Override
    public void enqueue(Integer element) {
        super.enqueue(element);
    }

    @Benchmarkable(nanoTime = 10_000_000)
    public void otherMethod(Integer x) {
        
    }

    public static int getInstances() {
        return numberOfInstancesMade;
    }
}
