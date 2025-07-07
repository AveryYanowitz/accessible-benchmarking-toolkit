package com.slc.tools.annotations;

/** Defines how often during benchmarking a new instance of this class is created.
 * 
 * ON_INIT: Performed once, when the benchmarking begins
 * PER_METHOD: Performed once for each method (validly) annotated with @Benchmarkable
 * PER_TEST_CASE: Performed once for each value of "size"
 * NEVER: Only execute static @Benchmarkable methods from this class
 * 
 */
public enum InitializationFrequency {
    ON_INIT,
    PER_METHOD,
    PER_TEST_CASE,
    NEVER,
}
