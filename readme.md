# Simple Benchmarking Library

This library allows Java programmers to benchmark their code at the function level. It is similar to, and inspired by, Java Microbenchmarking Harness; however, JMH is very heavy-duty and complex, and meant for professional engineers. This tool is instead geared towards beginners who only need an estimate of their code's effectiveness, rather than a completely rigorous diagnostic.

## Getting Started

(Requires Maven.)

Download this project, open it, and run "mvn clean install" in the command line. If this generates errors reading "method get____() not found," install the VSCode or VSCodium Project Lombok extension and run the command again. This will add the project to your local Maven repository. Then, in the projects you want to use it with, add the following text to your pom.xml file under \<dependencies\>:

    <groupId>com.slc.tools</groupId>
    <artifactId>benchmarking-tools</artifactId>
    <version>1.1.2</version>

(You may need to update the version number.) From there, you can import and call all of the classes and functions provided in the library.

## Features

### Benchmarking: API
At the time of writing, there is only a single benchmarking method, `benchmarkConsumer()`. This takes a Consumer<T> and a Stream<T> and runs the Consumer many times for each element of the Stream. It reports the averages in the form of a Stream<BenchmarkStats> (see below). Instead of a Stream, you may also pass an Iterable<T> or its subclasses, or a T[].

Each benchmark is tagged with the user-specified test name and a size. "Size" does not necessarily have to refer to the size of the object; this is simply the most common intended use-case. To get this value, the function must be provided a property name—which can refer to either a method or a field, indicated by a `true` or `false` value for `idIsMethod`, respectively. If `true`, the program will run the given method on each object and use its return value as the size; otherwise, it will grab the value of the field. If the method or field name is not valid, then the function will return null. **If the resulting value is not a number, "size" will be reported as null.**

### Benchmarking: Under the Hood

`benchmarkConsumer()` performs no benchmarking itself; instead, it makes repeated calls to `_singleTest()`. Understanding this function isn't necessary to use this library, but might be helpful if you get strange results. The function takes the provided Consumer<T> and a single piece of input, starts a clock, and begins running the Consumer on that input until the clock is up (or until its repetitions would exceed `Integer.MAX_VALUE`.) 

Because checking the clock is relatively expensive, the program doesn't check after every call to the Consumer. Instead, the user determines how often it checks via the `clockFrequency` value. Higher values will lead to greater accuracy at the potential cost of going over time; lower values will be less accurate, but more faithful to the intended runtime.

### Benchmarking: Results

As previously mentioned, the `benchmarkConsumer()` method returns a Stream of BenchmarkStats objects. Each of these objects records a wealth of information: the name of the test, the size, the number of clock checks, the number of loops between checks, the number of times the Consumer was run, the theoretical maximum duration, the actual duration (almost always longer), and the average time per Consumer call in milliseconds. 

(Note: `size` is stored as a Double object rather than a double primitive because it can be `null`, as described above.)

There is also a Jsonifier class which offers a few `jsonify` methods. These make use of the Jackson library to save Lists or Streams of BenchmarkStats. You can optionally specify a file name or full file path; if not, the default location is `src/main/output`, and the default file name is `results.json`. The JSON combines each list into a single object with two parameters: the test name (which should be the same in all elements of the list), and an array of DataFields, each of which corresponds to one of the BenchmarkStats from the original list.

**WARNING: If you run JSONify multiple times with the same file name (or with default parameters), it will overwrite previous saves.**