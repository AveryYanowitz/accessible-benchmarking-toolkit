package com.slc.tools.runners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.stream.Stream;

import com.slc.tools.annotations.Benchmarkable;
import com.slc.tools.util.BenchmarkStats;
import com.slc.tools.util.FormatUtils;

public class MethodRunner<C> {
    final Method _METHOD;
    final C _TARGET;
    final Stream<?> _DATA_TO_TEST;
    final Duration _MAX_DURATION;
    final int _CLOCK_FREQ;
    final String _TEST_NAME;
    final boolean _IS_STATIC, _NEEDS_ARGS;

    /**
     * Creates a new MethodRunner based around the given method, to be called on the given target with the given dataStream
     * @param method The method to be run with this MethodRunner instance
     * @param target The target to invoke the method on, or null if the method should construct a new target for each benchmark
     * @param dataStream The data to test this method on, or null if the method has no input arguments
     * @throws IllegalStateException
     */
    MethodRunner(Method method, C target, Stream<?> dataStream) throws IllegalStateException {
        Benchmarkable benchmark = method.getAnnotation(Benchmarkable.class);
        
        _METHOD = method;
        _MAX_DURATION = Duration.ofNanos(benchmark.nanoTime());
        _CLOCK_FREQ = benchmark.clockFrequency();
        _TEST_NAME = (benchmark.testName() == null) ? method.getName() : benchmark.testName();
        _IS_STATIC = Modifier.isStatic(_METHOD.getModifiers());
        _NEEDS_ARGS = method.getParameterCount() > 0;
        _DATA_TO_TEST = (dataStream == null) ? Stream.of("arbitrary non-null placeholder") : dataStream;

        if (_IS_STATIC) {
            _TARGET = null;
        } else {
            _TARGET = target;
        }
        
        _checkIfValid();
    }
    
    /**
     * Detects whether the given method is an instance or static method and calls the appropriate test function.
     * If it's an instance method, then this function will first create an object to invoke it on.
     * @param <C> The class that contains <code> method </code>
     * @param <T> The parameter that <code> method </code> takes
     * @param method The method to test
     * @param dataToTest A Stream of data to call <code> method </code> with
     * @return A Stream of BenchmarkStats representing the results of calling <code> method </code> on each element of <code> dataToTest </code>
     */
    @SuppressWarnings("unchecked")
    protected Stream<BenchmarkStats> benchmark() {        
        return _DATA_TO_TEST.map((Object streamMember) -> {
            try {
                C nonNullTarget;
                if (_TARGET == null && !_IS_STATIC) { 
                    // means we need to create a new instance on every invocation of benchmark()
                    nonNullTarget = (C) ClassRunner.createNewInstance(_METHOD.getDeclaringClass());
                } else {
                    nonNullTarget = _TARGET; // if static, null; otherwise, definitely not null
                }
                return _singleMethodTest(nonNullTarget, streamMember);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Takes one method, feeds it one input repeatedly, and times how long it takes.
     * @param <T> The type of the input
     * @param method The method to test
     * @param target The object on which to invoke <code> method </code>. If <code> method </code> is a static method, this is ignored (and should be null.)
     * @param input The input to feed to <code> method </code>
     * @param maxDuration The ideal maximum time for the benchmark to take; note that it may take longer, especially if clockFrequency is high
     * @param clockFrequency How many times to call the consumer between checking if maxDuration has elapsed yet
     * @param propertyName The name of the field or method of object that represents this run's unique ID
     * @param idIsMethod True if property name represents a method, false if it represents a field
     * @param testName An identifier for all the tests of the same consumer
     * @return A single BenchmarkStats object representing the results of this run
     * @throws ReflectiveOperationException
     */

    private <T> BenchmarkStats _singleMethodTest(C target, T input) throws ReflectiveOperationException {
        long maxNanoTime = _MAX_DURATION.toNanos();
        int clockChecks = 0;
        int completedLoops = 0;
        
        long startTime;
        if (_NEEDS_ARGS) {
            startTime = System.nanoTime();
            while ((System.nanoTime() - startTime) < maxNanoTime) {
                clockChecks++;
                for (int i = 0; i < _CLOCK_FREQ; i++) {
                    try {
                        _METHOD.invoke(target, input);
                    } catch (ReflectiveOperationException e) {
                        throw e;
                    } catch (Exception e) {
                        ClassRunner.printSkipMessage(_METHOD, e);
                        throw new ReflectiveOperationException(e.getMessage());
                    }
                    // increment completedLoops before asking if max has been reached
                    // to prevent extra loop from being performed and overflowing
                    if (++completedLoops == Integer.MAX_VALUE) {
                        break;
                    }
                }
            }
        } else {
            startTime = System.nanoTime();
            while ((System.nanoTime() - startTime) < maxNanoTime) {
                clockChecks++;
                for (int i = 0; i < _CLOCK_FREQ; i++) {
                    try {
                        _METHOD.invoke(target);
                    } catch (ReflectiveOperationException e) {
                        throw e;
                    } catch (Exception e) {
                        ClassRunner.printSkipMessage(_METHOD, e);
                        throw new ReflectiveOperationException(e.getMessage());
                    }
                    // increment completedLoops before asking if max has been reached
                    // to prevent extra loop from being performed and overflowing
                    if (++completedLoops == Integer.MAX_VALUE) {
                        break;
                    }
                }
            }
        }
        long nanosElapsed = System.nanoTime() - startTime;

        clockChecks++; // last check returned false, so it didn't increment
        Duration elapsedTime = Duration.ofNanos(nanosElapsed);
        Double size = FormatUtils.getPropertyByName(input, _TEST_NAME);
        return new BenchmarkStats(clockChecks, _CLOCK_FREQ, _MAX_DURATION, completedLoops, elapsedTime, size, _TEST_NAME);
    }

    /**
     * Checks the provided method to make sure it's accessible and has the correct number of parameters
     * @param method The method to check
     * @param instance Instance of <code> method </code>'s declaring class
     * @param expectedParamCount The number of parameters the method should have
     * @return boolean indicating whether the method is valid
     */
    private void _checkIfValid() throws IllegalArgumentException {
        int expectedParamCount = _NEEDS_ARGS ? 1 : 0;
        boolean paramCountCorrect = _METHOD.getParameterCount() == expectedParamCount;

        StringBuilder errors = new StringBuilder(_TEST_NAME);
        errors.append("has the following errors:\n");
        if (!paramCountCorrect) {
            throw new IllegalArgumentException("Wrong number of params: expected <"+expectedParamCount+"> but got <"+_METHOD.getParameterCount()+">");
        }

        if (_IS_STATIC && !_METHOD.canAccess(null)) {
            throw new IllegalArgumentException("Unable to access method in test "+_TEST_NAME);
        } else if (!_IS_STATIC) {
            Object instance = ClassRunner.createNewInstance(_METHOD.getDeclaringClass());
            if (instance == null || !_METHOD.canAccess(instance)) {
                throw new IllegalArgumentException("Unable to access method in test "+_TEST_NAME);
            }
        }
        
    }

}
