package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoInvasion extends AfkOnlyFlag {
    public FlagDoInvasion() {
        super("invasion", "Auto doing Invasion");
    }
}
