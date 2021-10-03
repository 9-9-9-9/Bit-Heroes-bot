package bh.bot.common.types.flags;

public class Flags {
    @SuppressWarnings("rawtypes")
    public static final FlagPattern[] allFlags = new FlagPattern[]{
            new FlagDoGvG(),
            new FlagDoInvasion(),
            new FlagDoExpedition(),
            new FlagDoPvp(),
            new FlagDoRaid(),
            new FlagDoTrials(),
            new FlagDoGauntlet(),
            new FlagDoWorldBoss(),
            new FlagExitAfterAmountOfSeconds(),
            new FlagShutdownAfterExit(),
            new FlagMuteNoti(),
            new FlagPrintHelpMessage(),
            new FlagPlayOnSteam(),
            new FlagPlayOnWeb(),
            new FlagSaveDebugImages(),
            new FlagShowDebugMessages(),
            new FlagProfileName(),
            new FlagExitAfkAfterIfWaitResourceGeneration(),
            new FlagCloseGameWindowAfterExit(),
            new FlagBribe(),
			new FlagAlterLoopInterval()
    };
}
