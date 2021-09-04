package bh.bot.common.utils;

import bh.bot.common.OS;

public class Extensions {
    public static String scriptFileName(String code) {
        return code + (OS.isWin ? ".bat" : ".sh");
    }
}