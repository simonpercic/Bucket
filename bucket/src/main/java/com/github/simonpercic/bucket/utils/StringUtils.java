package com.github.simonpercic.bucket.utils;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public final class StringUtils {

    private StringUtils() {
        // no instance
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }
}
