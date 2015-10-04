package ru.shishmakov.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Dmitriy Shishmakov
 */
public class StringUtil {

    /**
     * Replacement of version from {@link StringUtils#substring(String, int, int)}.
     * Was added  {@code "..."} to the end of string if end position less than length of original string.
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means
     *              count back from the end of the String by this many characters
     * @param end   the position to end at (exclusive), negative means
     *              count back from the end of the String by this many characters
     * @return substring from start position to end position,
     * {@code null} if null String input
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        String truncation = StringUtils.EMPTY;
        if (end < str.length()) {
            truncation = " ...";
        }else {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return StringUtils.EMPTY;
        }

        if (start < 0) {
            start = 0;
        }

        if (end < 0) {
            end = 0;
        }
        return str.substring(start, end) + truncation;
    }
}
