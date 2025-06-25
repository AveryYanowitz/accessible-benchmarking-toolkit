package com.slc.tools;

import java.time.Duration;

public class Benchmarker {
    private volatile int _completedCalls;
    private final Runnable _BENCHMARK_FUNCTION;

    public Benchmarker(Runnable functionToBenchmark) {
        _completedCalls = 0;
        _BENCHMARK_FUNCTION = () -> {
            functionToBenchmark.run();
            _completedCalls++;
        };
    }

    public static void main(String[] args) {
        Benchmarker benchmarker = new Benchmarker(() -> {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                System.out.println("Interrupted!");
            }
        });
        benchmarker.test();
    }

    public void test() {
        test(80, Duration.ofSeconds(100));
    }

    public void test(int numberOfLoops, Duration maxDuration) {
        long maxNanoTime = maxDuration.toNanos();
        int outerLoops, innerLoops;
        if (numberOfLoops < 20) {
            outerLoops = 1;
            innerLoops = numberOfLoops;
        } else {
            outerLoops = 10;
            innerLoops = numberOfLoops / 10;
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < outerLoops; i++) {
            if ((System.nanoTime() - startTime) < maxNanoTime) {
                for (int j = 0; j < innerLoops; j++) {
                    _BENCHMARK_FUNCTION.run();
                }
            } else {
                break;
            }
        }
        float elapsedTimeMillis = (System.nanoTime() - startTime) / 1_000_000.0f;
        float callsPerSecond = (_completedCalls / elapsedTimeMillis) * 1000;

        StringBuilder summaryMessage = new StringBuilder();
        summaryMessage.append("Executed ");
        summaryMessage.append(callsPerSecond);
        summaryMessage.append(" times per second (total: ");
        summaryMessage.append(_completedCalls);
        summaryMessage.append(" calls in ");
        summaryMessage.append(elapsedTimeMillis);
        summaryMessage.append(" ms)");

        System.out.println(summaryMessage.toString());

    }

}
