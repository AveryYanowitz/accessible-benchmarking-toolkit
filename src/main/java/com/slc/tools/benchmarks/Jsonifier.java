package com.slc.tools.benchmarks;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Jsonifier allows the user to save data in JSON format. */
public class Jsonifier {

    /** Copy of BenchmarkStats without testName field, so it can be extracted into the Jsonifier object */
    private static record DataField(Double size, int clockChecks, int loopsBetweenChecks, int loopsCompleted,
                                    Duration maxDuration, Duration actualTimeElapsed, double averageTimeMillis) {
        public DataField(BenchmarkStats baseStats) {
            this(baseStats.size(), baseStats.clockChecks(), baseStats.loopsBetweenChecks(),
            baseStats.loopsCompleted(), baseStats.maxDuration(), baseStats.actualTimeElapsed(),
            baseStats.getAverageTimeMillis());
        }
    }

    /** Packages a list of DataFields with the name of the test they represent */
    private static record DataPackage(String testName, DataField[] data) {    }

    private List<DataPackage> dataPackages;
    private File destinationFile;
    
    public Jsonifier() {
        dataPackages = new ArrayList<>();
        destinationFile = new File("src/output/results.json");
    }

    @SafeVarargs
    public Jsonifier(List<BenchmarkStats>... underlyingStats) {
        this();
        for (List<BenchmarkStats> stat : underlyingStats) {
            addToJson(stat);
        }
    }

    @SafeVarargs
    public Jsonifier(Stream<BenchmarkStats>... underlyingStats) {
        this();
        for (Stream<BenchmarkStats> stream : underlyingStats) {
            addToJson(stream.toList());
        }
    }

    public void addToJson(List<BenchmarkStats> statsToAdd) {
        String testName = statsToAdd.get(0).testName();
        DataField[] data = new DataField[statsToAdd.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = new DataField(statsToAdd.get(i));
        }
        dataPackages.add(new DataPackage(testName, data));
    }

    public void addToJson(BenchmarkStats singleStat) {
        List<BenchmarkStats> listWrapper = new ArrayList<>();
        listWrapper.add(singleStat);
        addToJson(listWrapper);
    }

    public void addToJson(Stream<BenchmarkStats> streamStats) {
        addToJson(streamStats.toList());
    }

    public void setFile(File file) {
        destinationFile = file;
    }

    public void setFile(String pathname) {
        destinationFile = new File(pathname);
    }

    public int size() {
        return dataPackages.size();
    }

    public void jsonify() throws StreamWriteException, DatabindException, IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.writeValue(destinationFile, dataPackages);
    }

}
