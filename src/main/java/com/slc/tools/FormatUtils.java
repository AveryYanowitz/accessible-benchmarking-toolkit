package com.slc.tools;

import java.time.Duration;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class FormatUtils {
    
    public static String formatFunction(String functionName) {
        StringBuilder sb = new StringBuilder();
        char[] chars = functionName.toCharArray();
        sb.append(Character.toUpperCase(chars[0]));

        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isLowerCase(ch)) {
                sb.append(ch);
            } else {
                sb.append(" ");
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    public static String formatDuration(Duration duration) {
        String fullStr = duration.toString(); // has extra chars we don't want
        String numberOnly = fullStr.substring(2, fullStr.length() - 1);
        String secs = " sec";
        if (!numberOnly.equals("1")) {
            secs += "s";
        }
        return numberOnly + secs;
    }

    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        Builder<T> builder = Stream.builder();
        for (T item : iterable) {
            builder.add(item);
        }
        return builder.build();
    }

    public static <T> Stream<T> toStream(T[] arr) {
        Builder<T> builder = Stream.builder();
        for (T item : arr) {
            builder.add(item);
        }
        return builder.build();
    }

}
