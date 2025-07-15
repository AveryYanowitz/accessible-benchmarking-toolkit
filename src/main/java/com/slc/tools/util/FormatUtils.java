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
    
    /**
     * Takes a function name written in lowerCamelCase or UpperCamelCase and converts it to Title Casing
     * @param functionName The String to convert; should be in camel case
     * @return The title-cased version of functionName (with spaces added between words)
     */
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

    /**
     * Duration objects have an ugly toString method, so this is a more visually pleasing version
     * @param duration The Duration to format
     * @return The better String version
     */
    public static String formatDuration(Duration duration) {
        String fullStr = duration.toString(); // has extra chars we don't want
        String numberOnly = fullStr.substring(2, fullStr.length() - 1);
        String secs = " sec";
        if (!numberOnly.equals("1")) {
            secs += "s";
        }
        return numberOnly + secs;
    }

    /**
     * Turns any Iterable into a Stream
     * @param <T> The type stored in the Iterable
     * @param iterable The iterable to convert
     * @return A Stream version of the iterable
     */
    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        Builder<T> builder = Stream.builder();
        for (T item : iterable) {
            builder.add(item);
        }
        return builder.build();
    }

    /**
     * Turns any array into a stream
     * @param <T> The type stored in the array
     * @param arr The array to convert
     * @return An array version of the iterable
     */
    public static <T> Stream<T> toStream(T[] arr) {
        Builder<T> builder = Stream.builder();
        for (T item : arr) {
            builder.add(item);
        }
        return builder.build();
    }

    /**
     * Read the given object and find the named property; if it's a field, return its value. If it's a method, invoke it on that object and return the result.
     * Returns null if the field isn't found, or if the result is not a number.
     * @param <T> The type of object to read from
     * @param object The object whose property should be read
     * @param propertyName The name of the property
     * @param searchMethods True if the property is a method, false if it's a field
     * @return The value of the property, or null if the property doesn't exist or isn't a number
     */
    public static <T> Double getPropertyByName(T object, String propertyName) {
        try {
            String value = null;
            boolean wasMethod = false;
            Method[] methods = object.getClass().getMethods();
            for (Method method : methods) {
                System.out.println("METHOD: "+method.getName());
                if (method.getName().equals(propertyName)) {
                    value = method.invoke(object).toString();
                    wasMethod = true;
                    break;
                }
            }
            if (!wasMethod) {
                Field field = object.getClass().getField(propertyName);
                value = field.get(object).toString();
            }
            return Double.parseDouble(value);
        } catch (ReflectiveOperationException e) {
            System.out.println("unable to get property name "+propertyName+" from object "+object+" because ROE: "+e.getMessage());
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            System.out.println("unable to get property name "+propertyName+" from object "+object+" because NFE: "+e.getMessage());
            return null;
        }
    }
}
