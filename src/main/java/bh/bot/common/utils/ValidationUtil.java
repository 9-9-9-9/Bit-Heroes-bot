package bh.bot.common.utils;

import java.util.regex.Pattern;

import static bh.bot.common.utils.StringUtil.isBlank;

public class ValidationUtil {
    private static final Pattern patternProfileName = Pattern.compile("^[a-z0-9\\-_]+$");

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidUserProfileName(String name) {
        if (isBlank(name))
            return false;
        return patternProfileName.matcher(name).matches();
    }
}