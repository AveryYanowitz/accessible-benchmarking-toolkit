package com.slc.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Benchmarkable {
    public int nanoTime() default 1_000_000_000;
    public int clockFrequency() default 15;
    public String idName() default "size";
    public String testName() default "";
    public boolean idIsMethod() default true;
    public OutputType outputTo() default OutputType.JSON;
}
