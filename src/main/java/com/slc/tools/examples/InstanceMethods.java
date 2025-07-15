package com.slc.tools.examples;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.slc.tools.annotations.BenchmarkSuite;
import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.annotations.Frequency;
import com.slc.tools.annotations.OutputType;
import com.slc.tools.runners.ClassRunner;


@BenchmarkSuite(outputTo = OutputType.JSON, whenToInstantiate = Frequency.PER_SIZE_VALUE)
public class InstanceMethods {
    private Queue<Integer> _q1, _q2;

    public InstanceMethods() {
        _q1 = new ArrayDeque<>();
        _q2 = new LinkedList<>();
    }
    
    @Benchmarkable(testName = "ArrayList Queue", idName = "intValue", idIsMethod = true)
    public void arrQueueBenchmark(int n) {
        _massQueueing(_q1, n);
    }

    @Benchmarkable(testName = "Linked Queue", idName = "intValue", idIsMethod = true)
    public void linkQueueBenchmark(int n) {
        _massQueueing(_q2, n);
    }
    
    private static void _massQueueing(Queue<Integer> q, int n) {
        for (int i = 0; i < n; i++) {
            q.add(i);
        }
        for (int i = 0; i < n; i++) {
            q.remove();
        }
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, ReflectiveOperationException {
        List<Integer> sizes = Sorters.getRandomIntList(10, 1, 25);
        ClassRunner.runBenchmarks(InstanceMethods.class, sizes);
    }

}
