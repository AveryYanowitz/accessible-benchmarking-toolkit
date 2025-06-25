package com.slc.tools;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 0)
@Warmup(iterations = 0)
@Measurement(iterations = 2, time = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JmhExample {
    static int x = 0;
    // @Benchmark
    public static void askClock(Blackhole bh) {
        bh.consume(System.currentTimeMillis());
    }

    // @Benchmark
    public static void notBenchmark(Blackhole bh) {
        int x = 2;
        int y = 1;
        int sum = x + y;
        bh.consume(sum);
        if ((sum ^ 0b101) == 2) {
            bh.consume(true);
        }
    }

    public static Object empty() {
        x++;
        return null;
    }

    @Benchmark
    public static void emptyLooper(Blackhole blackhole) {
        x++;
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}