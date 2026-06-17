package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoFishing extends AfkOnlyFlag {
    public FlagDoFishing() {
        super("bait", "Auto doing fishing bait");
    }
}
