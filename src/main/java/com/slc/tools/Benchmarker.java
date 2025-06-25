package com.slc.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Benchmarker {
    private volatile int _completedCalls;
    private final Callable<Object> _BENCHMARK_FUNCTION;
    
    public static void main(String[] args) {
        Benchmarker benchmarker = new Benchmarker(() -> null);
        benchmarker.test(1000, 2);
    }


    public Benchmarker(Callable<Object> functionToBenchmark) {
        _completedCalls = 0;
        _BENCHMARK_FUNCTION = () -> {
            functionToBenchmark.call();
            _completedCalls++;
            return null;
        };
    }

    public Benchmarker(Runnable functionToBenchmark) {
        _completedCalls = 0;
        _BENCHMARK_FUNCTION = Executors.callable(() -> {
            while (true) {
                functionToBenchmark.run();
                _completedCalls++;
            }
        }, null);
    }

    public void test(int durationInMillis, int iterationsToRun) {
        final ExecutorService execService = Executors.newSingleThreadExecutor();
        long startTime = System.nanoTime();
        for (int i = 0; i < iterationsToRun; i++) {
            _oneIter(durationInMillis, execService);
        }
        float elapsedTimeMillis = (System.nanoTime() - startTime) / 1_000_000.0f;
        float callsPerSecond = (_completedCalls / elapsedTimeMillis) * 1000;

        StringBuilder summaryMessage = new StringBuilder();
        summaryMessage.append("Executed ");
        summaryMessage.append(callsPerSecond);
        summaryMessage.append(" times per second (total runtime ");
        summaryMessage.append(elapsedTimeMillis);
        summaryMessage.append(" ms)");

        System.out.println(summaryMessage.toString());

        _expeditedShutdown(execService);
    }

    private void _oneIter(int durationInMillis, ExecutorService execService) {
        try {
            final Future<Object> f = execService.submit(_BENCHMARK_FUNCTION);
            f.get(durationInMillis, TimeUnit.MILLISECONDS);
        } catch (final Exception e) {
            return;
        }
    }

    private static void _expeditedShutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}
