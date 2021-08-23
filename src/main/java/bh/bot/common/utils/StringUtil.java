package bh.bot.common.utils;

public class StringUtil {
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }
}
