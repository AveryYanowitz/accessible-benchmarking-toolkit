package com.slc.tools.util;

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
import com.slc.tools.annotations.BenchmarkSuite;

/** Jsonifier allows the user to save data in JSON format. */
public class Jsonifier {

    /** Copy of BenchmarkStats without testName field, so it can be extracted into the Jsonifier object */
    private static record NamelessStats(Double size, int clockChecks, int loopsBetweenChecks, int loopsCompleted,
                                    Duration maxDuration, Duration actualTimeElapsed, double averageTimeMillis) {
        public NamelessStats(BenchmarkStats baseStats) {
            this(baseStats.size(), baseStats.clockChecks(), baseStats.loopsBetweenChecks(),
            baseStats.loopsCompleted(), baseStats.maxDuration(), baseStats.actualTimeElapsed(),
            baseStats.averageTimeNanos());
        }
    }

    /** Packages a list of NamelessStats together with the name of the test they represent */
    private static record DataField(String testName, NamelessStats[] data) {    }

    private List<DataField> dataFields;
    private File destinationFile;
    
    public Jsonifier() {
        dataFields = new ArrayList<>();
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

    public Jsonifier(String savePath) {
        this();
        setFile(savePath);
    }

    public void addToJson(List<BenchmarkStats> statsToAdd) {
        String testName = BenchmarkStats.getTestNameFromList(statsToAdd);
        NamelessStats[] data = new NamelessStats[statsToAdd.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = new NamelessStats(statsToAdd.get(i));
        }
        dataFields.add(new DataField(testName, data));
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
        return dataFields.size();
    }

    public void jsonify() throws StreamWriteException, DatabindException, IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.writeValue(destinationFile, dataFields);
    }

    public static Jsonifier getJsonifier(Class<?> clazz) {
        String savePath;
        BenchmarkSuite classAnno = clazz.getAnnotation(BenchmarkSuite.class);
        if (classAnno != null) {
            savePath = classAnno.saveLocation() + "/" + classAnno.fileName();
        } else {
            savePath = "src/main/output/results.json";
        }
        return new Jsonifier(savePath);
    }

}
