# Simple Benchmarking Library
This library allows Java programmers to benchmark their code at the function level. It is similar to, and inspired by, Java Microbenchmarking Harness; however, JMH is very heavy-duty and complex, and meant for professional engineers. This tool is instead geared towards beginners who only need an estimate of their code's effectiveness, rather than a completely rigorous diagnostic.

## Getting Started
(Requires Maven.)

Download this project, open it, and run "mvn clean install" in the command line. If this generates errors reading "method get____() not found," install the VSCode or VSCodium Project Lombok extension and run the command again. This will add the project to your local Maven repository. Then, in the projects you want to use it with, add the following text to your pom.xml file under \<dependencies\>:

    <dependency>
        <groupId>com.slc.tools</groupId>
        <artifactId>benchmarking-tools</artifactId>
        <version>1.1.2</version>
    </dependency>

(You may need to update the version number.) From there, you can import and call all of the classes and functions provided in the library.

**Disclaimer: This project relies on Ema Sestakova's stack/queue implementation. I downloaded it, changed the group and artifact IDs, and ran `mvn install` on it. I'm not sure whether this will work on someone else's computer. Please let me know if not!**

## `annotations` Package
### @Benchmarkable
The @Benchmarkable annotation allows you to mark a particular method for benchmarking, and optionally define the benchmark's parameters if they differ from the original. These parameters are:

- **int nanoTime:** The maximum amount of time (in nanoseconds) to run this benchmark for. Defaults to 1 billion (i.e. 1 second).
- **int clockFrequency:** How many iterations should pass between checking if the maximum time has elapsed; larger values provide more accurate testing at the cost of potentially taking much longer than the expected maximum time. Defaults to 15.
- **idName:** The field or method to get the "size" property from. Defaults to "size".
- **idIsMethod:** Whether idName refers to a method, in which case it will be populated by the return value of that method. Defaults to "true".
- **testName:** A unique identifier for *all* tests performed on this method. Defaults to an empty string.

Methods with this annotation must be public members of a public class, or the benchmark will be skipped. Synthetic methods are also skipped.

**Note: all of the @Benchmarkable methods in one class are run with the same data, so they must all share the same input type.**

### @BenchmarkSuite and OutputType
If you have a class with a series of @Benchmarkable methods, you can mark the class with the @BenchmarkSuite annotation to customize the data reporting process for that class as follows:

- **Frequency whenToInstantiate:** If the class contains instance methods, how often to instantiate a new object; valid values are NEVER (in which case instance methods will be skipped), ON_INIT (once), or PER_METHOD (once for each method).
- **OutputType outputTo:** What the program should do with the data it generates. `PRINT` simply prints it to `System.out`, with no long-term storage; `RETURN` returns it as a List\<BenchmarkStats> for use elsewhere in the program; and `JSON` saves it to a JSON file. Defaults to `OutputType.JSON`.
- **String saveLocation:** File path indicating where to save a JSON file; has no effect if using `PRINT` or `RETURN` as output type. Defaults to "src/main/output".
- **String fileName:** File name for JSON file; has no effect if using `PRINT` or `RETURN` as output type. Defaults to "results.json".

## `benchmarks` Package
### ClassRunner and MethodRunner
`ClassRunner.java` provides the means to run all of the benchmark methods in a particular class. (See **`annotations` Package** below to learn how to create a benchmark method.) From an API standpoint, this is very simple: simply call runBenchmarks with the class you have in mind and a list of data. This data will be run through every \@Benchmarkable method in the provided class. If no `@BenchmarkSuite` annotation is present, or if it does not specify how to report data, it will be saved to a JSON file. (See `Jsonifier` section below for more information.) 

Under the hood, ClassRunner goes through each Benchmark method of the provided class. If the method is static, then it makes a call to `MethodRunner.benchmarkStaticMethod()`. If it's an instance method, its behavior instead depends on the \@BenchmarkSuite annotation on the method's declaring class, and in particular, on the value of `whenToInstantiate`. **If this annotation is not present, or this value is not specified, the method will be skipped. If the class does not have a public no-args constructor, the method will be skipped.** There are three possible values for this value, given by the Frequency enum:

1. Frequency.NEVER: The instance method is skipped entirely.
2. Frequency.ON_INIT: Upon calling `ClassRunner.runBenchmarks()`, the program will instantiate an object of the method's declaring class. This will be passed to every test.
3. Frequency.PER_METHOD: `ClassRunner.runBenchmarks()` will instantiate an object of the declaring class for each method separately.

Eventually, I want to add a Frequency.PER_TEST_CASE option, which will instantiate an object once for each element in the dataToTest list. I thought about a Frequency.PER_CALL option, but that doesn't work because it would interfere with the timing library, since there is no instantaneous instantiation.

### LambdaRunner
There are two benchmarking methods provided for benchmarking lambda functions: `benchmarkConsumable()` and `benchmarkFunction()`. The latter is just a wrapper around the forumer, which takes a Stream\<T> and runs its Consumer many times for each element of the Stream. It reports the averages in the form of a Stream\<BenchmarkStats> (see **Results** below). Instead of a Stream\<T>, you may also pass an Iterable\<T> or its subclasses, or a T[], which will be converted to a Stream\<T>.

Each benchmark is tagged with the user-specified test name and a size. "Size" does not necessarily have to refer to the size of the object; this is simply the most common intended use-case. To get this value, the function must be provided a property name—which can refer to either a method or a field, indicated by a `true` or `false` value for `idIsMethod`, respectively. If `true`, the program will run the given method on each object and use its return value as the size; otherwise, it will grab the value of the field. If the method or field name is not valid, then the function will return null. **If the resulting value is not a number, "size" will be reported as null.**

### Single Tests
`LambdaRunner.singleConsumerTest()` and `MethodRunner.singleMethodTest()` are very similar. Understanding them is not a requirement for using this API, but it may be helpful if you get strange results. Each function takes a Consumer or Method and a single input, starts a clock, and begins running the Consumer/Method on that input until the clock is up (or until its repetitions would exceed `Integer.MAX_VALUE`.) 

Because checking the clock is relatively slow, the program doesn't check after every call. Instead, the user determines how often it checks via the `clockFrequency` value when calling a function (for Consumers/Functions) or in the annotation (for methods). Higher values will lead to greater accuracy at the potential cost of going over time; lower values will be less accurate, but more faithful to the intended runtime.

## `util` Package
### BenchmarkStats
BenchmarkStats are a convenient record class that bundle together the results of benchmarking one algorithm on one input. The `toString()` method has been overridden to provide a more print-friendly output, and the `isComplete()` method verifies that the BenchmarkStats object was created correctly (i.e. with no null or impossible values.) It's mostly intended for unit testing.

### Jsonifier
As previously mentioned, the `benchmarkConsumable()` method returns a Stream of BenchmarkStats objects. Each of these objects records a wealth of information: the name of the test, the size, the number of clock checks, the number of loops between checks, the number of times the Consumer was run, the theoretical maximum duration, the actual duration (almost always longer), and the average time per Consumer call in milliseconds. 

(Note: `size` is stored as a Double object rather than a double primitive because it can be `null`, as described above.)

Instead of printing out this data to `System.out`, you may want to save it for future use. This package provides the Jsonifier class to do exactly that. To use it, simply create a Jsonifier and supply it—either at construction or later on—with any combination of BenchmarkStats, List\<BenchmarkStats>, or Stream\<BenchmarkStats>. You can also give it a file or file name, which will change the default file `src/main/output/results.json`. Once you have everything added, call the `.jsonify()` method to save it.

This class relies on the Jackson library for the file writing. The format is as follows:

    [
        {
            testName: "name of test 1",
            data: [ array of {NamelessStats objects} ]
        },
        {
            testName: "name of test 2",
            data: [ array of {NamelessStats objects} ]
        },
        ...,
    ]

NamelessStats is very similar to BenchmarkStats, except that it lacks the testName field. An array of NamelessStats objects are then stored with a DataField, which *does* have a testName, and the Jsonifier stores a list of DataPackages that it writes to the file.

**WARNING: If you run JSONify multiple times with the same file name (or with default parameters), it will overwrite previous saves.**