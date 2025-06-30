package com.slc.tools;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

@Getter
public class Jsonifier {
    
    @Getter
    private static class DataOnly {
        final Double size;
        final int clockChecks, loopsBetweenChecks, loopsCompleted;
        final Duration maxDuration, actualTimeElapsed;
        final double averageTimeMillis;

        DataOnly(BenchmarkStats baseStats) {
            size = baseStats.getSize();
            clockChecks = baseStats.getClockChecks();
            loopsBetweenChecks = baseStats.getLoopsBetweenChecks();
            loopsCompleted = baseStats.getLoopsCompleted();
            maxDuration = baseStats.getMaxDuration();
            actualTimeElapsed = baseStats.getActualTimeElapsed();
            averageTimeMillis = baseStats.getAverageTimeMillis();
        }
    }

    public final String testName;
    public final List<DataOnly> data;

    private Jsonifier(List<BenchmarkStats> underlyingStats) {
        testName = underlyingStats.get(0).getTestName();
        data = new ArrayList<>();
        for (BenchmarkStats stats : underlyingStats) {
            data.add(new DataOnly(stats));
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
        for (List<BenchmarkStats> list : results) {
            Jsonifier entry = new Jsonifier(list);
            om.writeValue(jsonFile, entry);
        }
    }

}
