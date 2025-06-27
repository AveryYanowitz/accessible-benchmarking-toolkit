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

        this.averageTimeMillis = loopsCompleted / (double) actualTimeElapsed.toMillis();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Independent Variable:                ");
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

        File f = new File("src/main/resources/testing.json");
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.writeValue(f, results);
    }
}
