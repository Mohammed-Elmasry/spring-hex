package com.springhex.util;

public final class StringUtils {

    private StringUtils() {}

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String pluralize(String str) {
        if (str == null || str.isEmpty()) return str;
        if (str.endsWith("y")) {
            return str.substring(0, str.length() - 1) + "ies";
        } else if (str.endsWith("s") || str.endsWith("x") || str.endsWith("ch") || str.endsWith("sh")) {
            return str + "es";
        }
        return str + "s";
    }
}
