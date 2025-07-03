package com.slc.tools;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.slc.tools.benchmarks.BenchmarkingFuncs;

public class BenchmarkTests {
    
    public static class Foo {
        public static int bar(String x) {
            return x.length();
        }
    }

    @Test
    public void benchmarkMethodTest() throws SecurityException, NoSuchMethodException {
        Foo foo = new Foo();
        Class<?> clazz = "abc".getClass();
        Method method = foo.getClass().getDeclaredMethod("bar", clazz);
        Stream<String> dataToTest = Stream.of("lorem", "ipsum", "dolor", "sit", "amet");

        try {
            BenchmarkingFuncs.benchmarkMethod(method, dataToTest, Duration.ofSeconds(1), 15, "length", true, "length test");
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }
    }
}
