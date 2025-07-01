package com.slc.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Benchmark {
    public int maxDurationNanos() default 1_000_000_000;
    public int clockFrequency() default 15;
    public String idSource() default "size";
    public boolean idIsMethod() default true;
}
