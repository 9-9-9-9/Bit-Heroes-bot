package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoWorldBoss extends AfkOnlyFlag {
    public FlagDoWorldBoss() {
        super("boss", "Auto doing World Boss");
    }
}
