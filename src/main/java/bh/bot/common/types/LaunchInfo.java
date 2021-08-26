package bh.bot.common.types;

import bh.bot.app.AbstractApplication;

public class LaunchInfo {
    public AbstractApplication instance;
    public String[] arguments;
    public int exitAfterXSecs;
    public boolean enableDebugMessages;
    public boolean disableTelegramNoti;
    public boolean displayHelp;
    public boolean enableSavingDebugImages;
    public boolean eInvasion;
    public boolean eTrials;
    public boolean ePvp;
    public boolean eWorldBoss;
    public boolean eRaid;
    public ScreenResolutionProfile screenResolutionProfile;

    public LaunchInfo(AbstractApplication instance, String[] args) {
        this.instance = instance;
        this.arguments = args;
    }
}
