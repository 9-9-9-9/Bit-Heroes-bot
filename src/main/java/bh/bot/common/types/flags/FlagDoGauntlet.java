package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoGauntlet extends AfkOnlyFlag {
    public FlagDoGauntlet() {
        super("gauntlet", "Auto doing Gauntlet");
    }
}
