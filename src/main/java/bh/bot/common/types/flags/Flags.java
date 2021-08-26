package bh.bot.common.types.flags;

public class Flags {
    public static final FlagPattern[] allFlags = new FlagPattern[]{
            new FlagDoInvasion(),
            new FlagDoPvp(),
            new FlagDoRaid(),
            new FlagDoTrials(),
            new FlagDoWorldBoss(),
            new FlagExitAfterAmountOfSeconds(),
            new FlagMuteNoti(),
            new FlagPrintHelpMessage(),
            new FlagSteamResolution800x480(),
            new FlagWebResolution800x520(),
            new FlagSaveDebugImages(),
            new FlagShowDebugMessages(),
    };
}
