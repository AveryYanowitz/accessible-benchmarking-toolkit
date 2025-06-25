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
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 20)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JmhExample {
    @Benchmark
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

    // @Benchmark
    public static void empty() {

    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}