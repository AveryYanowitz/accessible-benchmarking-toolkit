package com.slc.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BenchmarkSuite {
    public Frequency whenToInstantiate() default Frequency.NEVER;
    public OutputType outputTo() default OutputType.JSON;
    public String saveLocation() default "src/main/output";
    public String fileName() default "results.json";
}
