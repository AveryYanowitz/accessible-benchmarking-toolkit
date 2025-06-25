package com.slc.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyAttempt {
    static int iterations = 0;
    static int whatever = 0;
    public static void main(String[] args) {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            final Future<Object> f = service.submit(() -> {
                while (iterations <= Integer.MAX_VALUE) {
                    long x = System.currentTimeMillis();
                    if ((x ^ 0) == (x ^ 1)) { // impossible
                        whatever++;
                    }
                    iterations++;
                }
                return null;
            });

            Thread.sleep(1000);
            f.cancel(true);
            
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Num iterations: "+iterations);
            service.shutdown();
        }
    }

}
