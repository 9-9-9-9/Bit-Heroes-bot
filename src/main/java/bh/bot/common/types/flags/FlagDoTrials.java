package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoTrials extends AfkOnlyFlag {
    public FlagDoTrials() {
        super("trials", "Auto doing Trials");
    }
}
