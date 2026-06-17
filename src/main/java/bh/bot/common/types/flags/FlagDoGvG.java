package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoGvG extends AfkOnlyFlag {
    public FlagDoGvG() {
        super("gvg", "Auto doing GvG");
    }
}
