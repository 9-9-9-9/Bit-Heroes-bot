package bh.bot.common.utils;

import bh.bot.common.exceptions.InvalidDataException;

import java.util.regex.Pattern;

public class StringUtil {
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    public static boolean isTrue(String text) {
        if (text == null)
            return false;
        text = text.trim().toLowerCase();
        return text.equals("true") || text.equals("yes") || text.equals("y");
    }

    private static final Pattern pAllDigits = Pattern.compile("\\d+");

    public static int parseTimeToSec(String time) {
        time = time.trim().toLowerCase();

        if (pAllDigits.matcher(time).matches())
            return Integer.parseInt(time);

        final InvalidDataException invalidDataException = new InvalidDataException("Invalid time format: %s", time);

        try {
            int resultInSec = 0;

            String[] spl;
            if (time.contains("d")) {
                spl = time.split("d");
                if (spl.length > 2 || !pAllDigits.matcher(spl[0]).matches())
                    throw invalidDataException;
                resultInSec += 86_400 * Integer.parseInt(spl[0]);
                if (spl.length == 1)
                    return resultInSec;
                time = spl[1];
            }

            if (time.contains("h")) {
                spl = time.split("h");
                if (spl.length > 2 || !pAllDigits.matcher(spl[0]).matches())
                    throw invalidDataException;
                resultInSec += 3_600 * Integer.parseInt(spl[0]);
                if (spl.length == 1)
                    return resultInSec;
                time = spl[1];
            }

            if (time.contains("m")) {
                spl = time.split("m");
                if (spl.length > 2 || !pAllDigits.matcher(spl[0]).matches())
                    throw invalidDataException;
                resultInSec += 60 * Integer.parseInt(spl[0]);
                if (spl.length == 1)
                    return resultInSec;
                time = spl[1];
            }

            if (time.contains("s")) {
                if (!time.endsWith("s"))
                    throw invalidDataException;
                time = time.substring(0, time.length() - 1);
            }

            resultInSec += Integer.parseInt(time);

            return resultInSec;
        } catch (NumberFormatException ex) {
            throw invalidDataException;
        }
    }
}