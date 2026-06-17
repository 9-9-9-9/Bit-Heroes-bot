package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 1000)
public class FlagDoExpedition extends AfkOnlyFlag {
    public FlagDoExpedition() {
        super("expedition", "Auto doing Expedition");
    }
}
