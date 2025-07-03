package com.slc.tools.benchmarks;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

@Getter
public class Jsonifier {

    private static record DataField(Double size, int clockChecks, int loopsBetweenChecks, int loopsCompleted,
                                    Duration maxDuration, Duration actualTimeElapsed, double averageTimeMillis) {
        public DataField(BenchmarkStats baseStats) {
            this(baseStats.size(), baseStats.clockChecks(), baseStats.loopsBetweenChecks(),
            baseStats.loopsCompleted(), baseStats.maxDuration(), baseStats.actualTimeElapsed(),
            baseStats.getAverageTimeMillis());
        }
    }

    public final String testName;
    public final List<DataField> data;

    private Jsonifier(List<BenchmarkStats> underlyingStats) {
        testName = underlyingStats.get(0).testName();
        data = new ArrayList<>();
        for (BenchmarkStats stats : underlyingStats) {
            data.add(new DataField(stats));
        }
    }

    @SafeVarargs
    public static void jsonify(List<BenchmarkStats>... results) throws IOException {
        jsonify(new File("src/output/results.json"), results);
    }

    @SafeVarargs
    public static void jsonify(String fileName, List<BenchmarkStats>... results) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        StringBuilder filepath = new StringBuilder("src/output/");
        filepath.append(fileName);
        if (!fileName.endsWith(".json")) {
            filepath.append(".json");
        }

        jsonify(new File(filepath.toString()), results);

    }
    
    @SafeVarargs
    public static void jsonify(File jsonFile, List<BenchmarkStats>... results) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        List<Jsonifier> jsonifiedResults = new ArrayList<>();
        for (List<BenchmarkStats> list : results) {
            Jsonifier entry = new Jsonifier(list);
            jsonifiedResults.add(entry);
        }
        om.writeValue(jsonFile, jsonifiedResults);
    }

    @SafeVarargs
    public static void jsonify(Stream<BenchmarkStats>... results) throws IOException {
        jsonify(new File("src/output/results.json"), results);
    }

    @SafeVarargs
    public static void jsonify(String fileName, Stream<BenchmarkStats>... results) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        StringBuilder filepath = new StringBuilder("src/output/");
        filepath.append(fileName);
        if (!fileName.endsWith(".json")) {
            filepath.append(".json");
        }

        jsonify(new File(filepath.toString()), results);

    }
    
    @SafeVarargs
    public static void jsonify(File jsonFile, Stream<BenchmarkStats>... results) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        List<Jsonifier> jsonifiedResults = new ArrayList<>();
        for (Stream<BenchmarkStats> stream : results) {
            List<BenchmarkStats> list = stream.collect(Collectors.toList());
            Jsonifier entry = new Jsonifier(list);
            jsonifiedResults.add(entry);
        }
        om.writeValue(jsonFile, jsonifiedResults);
    }

}
