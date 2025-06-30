package com.slc.tools;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

@Getter
public class BenchmarkStats {

    private final String indepVar, testName;
    private final int clockChecks, loopsBetweenChecks, loopsCompleted;
    private final double averageTimeMillis;
    private final Duration maxDuration, actualTimeElapsed;

    public BenchmarkStats(int clockChecks, int loopsBetweenChecks, Duration maxDuration,
                    int loopsCompleted, Duration actualTimeElapsed, String indepVar, String testName) {

        this.testName = testName;
        this.indepVar = indepVar;
        this.clockChecks = clockChecks;
        this.loopsBetweenChecks = loopsBetweenChecks;
        this.maxDuration = maxDuration;
        this.loopsCompleted = loopsCompleted;
        this.actualTimeElapsed = actualTimeElapsed;

        this.averageTimeMillis = (double) actualTimeElapsed.toMillis() / loopsCompleted;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Independent Variable:     ");
        sb.append(indepVar);
        sb.append("\n\n");

        sb.append("Clock Checks:             ");
        sb.append(clockChecks);
        sb.append("\n");

        sb.append("Loops Between Checks:     ");
        sb.append(loopsBetweenChecks);
        sb.append("\n");

        sb.append("Maximum Duration Set:     ");
        sb.append(StringUtils.formatDuration(maxDuration));
        sb.append("\n");

        sb.append("Total Loops Completed:    ");
        sb.append(loopsCompleted);
        sb.append("\n");

        sb.append("Total Time Elapsed:       ");
        sb.append(StringUtils.formatDuration(actualTimeElapsed));
        sb.append("\n");

        sb.append("Average Time Per Call:    ");
        sb.append(averageTimeMillis);
        sb.append(" ms \n");

        return sb.toString();
    }

    public static void jsonify(List<BenchmarkStats> results) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        
        File jsonFile = new File("src/main/resources/default.json");
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.writeValue(jsonFile, results);
    }

    public static void jsonify(List<BenchmarkStats> results, String fileName) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        StringBuilder filepath = new StringBuilder("src/main/resources/");
        filepath.append(fileName);
        if (!fileName.endsWith(".json")) {
            filepath.append(".json");
        }

        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.writeValue(new File(filepath.toString()), results);
    }
    
    public static void jsonify(List<BenchmarkStats> results, File jsonFile) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.writeValue(jsonFile, results);
    }
}
