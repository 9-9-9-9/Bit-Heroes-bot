package bh.bot.common.utils;

import bh.bot.common.exceptions.InvalidDataException;

import java.util.regex.Pattern;

public class TimeUtil {
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

    public static String niceTimeLong(int sec) {
        int d = sec / 86400;
        sec -= d * 86400;
        int h = sec / 3600;
        sec -= h * 3600;
        int m = sec / 60;
        sec -= m * 60;

        StringBuilder sb = new StringBuilder();
        if (d > 0)
            sb.append(d).append(" day").append(d > 1 ? "s " : " ");
        if (h > 0)
            sb.append(h).append(" hours ");
        if (m > 0)
            sb.append(m).append(" minute").append(m > 1 ? "s " : " ");
        sb.append(sec).append("seconds");
        return sb.toString();
    }
}
