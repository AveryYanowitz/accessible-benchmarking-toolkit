package com.slc.tools.utilityclasses;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;

import edu.slc.cs.stack_queue_comparison.ArrayDequeQueue;

@BenchmarkSuite(whenToInstantiate = Frequency.ON_INIT, outputTo = OutputType.RETURN)
public class ArrDequeWrapper extends ArrayDequeQueue<Integer> {
    static int numberOfInstancesMade = 0;
    
    public ArrDequeWrapper() {
        numberOfInstancesMade++;
    }

    @Benchmarkable(nanoTime = 10_000_000)
    @Override
    public void enqueue(Integer element) {
        super.enqueue(element);
    }

    public static int getInstances() {
        return numberOfInstancesMade;
    }
}
