package com.slc.tools.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class FormatUtils {

    /**
     * Utility function to easily convert Functions into Consumers
     * @param <T> The input type of the Consumer
     * @param <R>The return type of the original function
     * @param functionToConvert The function that you 
     * @return A Consumer wrapper around the provided function
     */
    public static <T, R> Consumer<T> toConsumer(Function<T,R> functionToConvert) {
        return ((T input) -> functionToConvert.apply(input));
    }
    
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

    public static <T> Double getPropertyByName(T object, String propertyName, boolean searchMethods) {
        try {
            String value;
            if (searchMethods) {            
                Method method = object.getClass().getMethod(propertyName);
                method.setAccessible(true);
                value = method.invoke(object).toString();
            } else {
                Field field = object.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                value = field.get(object).toString();
            }
            return Double.parseDouble(value);
        } catch (ReflectiveOperationException | NumberFormatException e) {
            return null;
        }
    }
}
