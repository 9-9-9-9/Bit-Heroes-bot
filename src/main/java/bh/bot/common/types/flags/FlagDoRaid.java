package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoRaid extends AfkOnlyFlag {
    public FlagDoRaid() {
        super("raid", "Auto doing Raid");
    }
}
