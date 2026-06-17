package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoPvp extends AfkOnlyFlag {
    public FlagDoPvp() {
        super("pvp", "Auto doing PVP");
    }
}
