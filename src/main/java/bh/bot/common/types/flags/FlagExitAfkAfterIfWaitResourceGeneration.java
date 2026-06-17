package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 5)
public class FlagExitAfkAfterIfWaitResourceGeneration extends AfkOnlyFlag {
    public FlagExitAfkAfterIfWaitResourceGeneration() {
        super("ear", "Exit AFK if no more task to do (all out of turns and wait for resource generation)");
    }
}
