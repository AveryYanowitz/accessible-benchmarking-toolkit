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

## `benchmarks` Package
### Consumers and Functions
There are three benchmarking methods, `benchmarkConsumable()`, `benchmarkFunction()`, and `benchmarkMethod()`. The first two are the subject of this section; the third is discussed in the section on the Annotations package.

The `benchmarkFunction()` method is provided for convenience; it wraps the given Function\<T, R> in a Consumer\<T> and calls `benchmarkConsumable()` with it. That method takes a Stream\<T> and runs its Conusmer many times for each element of the Stream. It reports the averages in the form of a Stream\<BenchmarkStats> (see **Results** below). Instead of a Stream\<T>, you may also pass an Iterable\<T> or its subclasses, or a T[], which will be converted to a Stream\<T>.

Each benchmark is tagged with the user-specified test name and a size. "Size" does not necessarily have to refer to the size of the object; this is simply the most common intended use-case. To get this value, the function must be provided a property name—which can refer to either a method or a field, indicated by a `true` or `false` value for `idIsMethod`, respectively. If `true`, the program will run the given method on each object and use its return value as the size; otherwise, it will grab the value of the field. If the method or field name is not valid, then the function will return null. **If the resulting value is not a number, "size" will be reported as null.**

### Under the Hood
`benchmarkConsumable()` performs no benchmarking itself; instead, it makes repeated calls to `_singleTest()`. Understanding this function isn't necessary to use this library, but might be helpful if you get strange results. The function takes the provided Consumer<T> and a single piece of input, starts a clock, and begins running the Consumer on that input until the clock is up (or until its repetitions would exceed `Integer.MAX_VALUE`.) 

Because checking the clock is relatively expensive, the program doesn't check after every call to the Consumer. Instead, the user determines how often it checks via the `clockFrequency` value. Higher values will lead to greater accuracy at the potential cost of going over time; lower values will be less accurate, but more faithful to the intended runtime.

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

## `annotations` Package
### @Benchmarkable
The @Benchmarkable annotation allows you to mark a particular method for benchmarking, and optionally define the benchmark's parameters if they differ from the original. These parameters are:

- **int nanoTime:** The maximum amount of time (in nanoseconds) to run this benchmark for. Defaults to 1 billion (i.e. 1 second).
- **int clockFrequency:** How many iterations should pass between checking if the maximum time has elapsed; larger values provide more accurate testing at the cost of potentially taking much longer than the expected maximum time. Defaults to 15.
- **idName:** The field or method to get the "size" property from. Defaults to "size".
- **idIsMethod:** Whether idName refers to a method, in which case it will be populated by the return value of that method. Defaults to "true".
- **testName:** A unique identifier for *all* tests performed on this method. Defaults to an empty string.

Methods with this annotation **must** be static, public, and a member of a public class. If it does not meet all three of these criteria, the benchmark will be skipped.

### @BenchmarkSuite and OutputType
If you have a class with a series of @Benchmarkable methods, you can mark the class with the @BenchmarkSuite annotation to customize the data reporting process for that class as follows:

- **OutputType outputTo:** What the program should do with the data it generates. `PRINT` simply prints it to `System.out`, with no long-term storage; `RETURN` returns it as a List\<BenchmarkStats> for use elsewhere in the program; and `JSON` saves it to a JSON file. Defaults to `OutputType.JSON`.
- **String saveLocation:** File path indicating where to save a JSON file; has no effect if using `PRINT` or `RETURN` as output type. Defaults to "src/main/output".
- **String fileName:** File name for JSON file; has no effect if using `PRINT` or `RETURN` as output type. Defaults to "results.json".


### Runner
The Runner class, and in particular, the `runBenchmarks()` method, is what allows @Benchmarkable to do its magic. To use it, simply call the method somewhere in your code, providing two arguments: the class containing `@Benchmarkable` methods, and the data you want to benchmark these methods with. Unlike `benchmarkConsumable()` and `benchmarkFunction`, this data must be in a List instead of a Stream. This is because it needs to be read multiple times, once for each method annotated with `@Benchmarkable`.

**Note: all of the @Benchmarkable methods in one class are run with the same data, so they must all share the same input type.**