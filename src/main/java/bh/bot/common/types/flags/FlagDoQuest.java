package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoQuest extends AfkOnlyFlag {
    public FlagDoQuest() {
        super("quest", "Auto doing Quest");
    }
}
