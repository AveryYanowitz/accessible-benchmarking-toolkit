package com.slc.tools.examples;

import java.io.IOException;
import java.util.List;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.runners.ClassRunner;

import edu.slc.cs.stack_queue_comparison.ArrayListQueue;
import edu.slc.cs.stack_queue_comparison.LinkQueue;
import edu.slc.cs.stack_queue_comparison.Queue;

@BenchmarkSuite(outputTo = OutputType.JSON, whenToInstantiate = Frequency.PER_SIZE_VALUE)
public class InstanceMethods {
    private Queue<Integer> _q1, _q2;

    public InstanceMethods() {
        _q1 = new ArrayListQueue<>();
        _q2 = new LinkQueue<>();
    }
    
    @Benchmarkable(testName = "ArrayList Queue", idName = "intValue", idIsMethod = true)
    public void arrListBenchmark(int n) {
        _massQueueing(_q1, n);
    }

    @Benchmarkable(testName = "Linked Queue", idName = "intValue", idIsMethod = true)
    public void linkEnqueue(int n) {
        _massQueueing(_q2, n);
    }
    
    private static void _massQueueing(Queue<Integer> q, int n) {
        for (int i = 0; i < n; i++) {
            q.enqueue(i);
        }
        for (int i = 0; i < n; i++) {
            q.dequeue();
        }
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, ReflectiveOperationException {
        List<Integer> sizes = Sorters.getRandomIntList(10, 1, 25);
        ClassRunner.runBenchmarks(InstanceMethods.class, sizes);
    }

}
