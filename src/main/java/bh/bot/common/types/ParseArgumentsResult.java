package bh.bot.common.types;

import bh.bot.app.AbstractApplication;
import bh.bot.common.types.flags.FlagPattern;

import java.util.ArrayList;

public class ParseArgumentsResult {
    public final Class<? extends AbstractApplication> applicationClass;
    public final String[] arguments;
    @SuppressWarnings("rawtypes")
    public final ArrayList<FlagPattern> usingFlags;
    public int exitAfterXSecs;
    public int timerLoopMain;
    public boolean exitAfkIfWaitForResourceGeneration;
    public boolean shutdownAfterExit;
    public boolean closeGameWindowAfterExit;
    public boolean enableDebugMessages;
    public boolean disableTelegramNoti;
    public boolean displayHelp;
    public boolean enableSavingDebugImages;
    public boolean eInvasion;
    public boolean eExpedition;
    public boolean eGvg;
    public boolean eTrials;
    public boolean eGauntlet;
    public boolean ePvp;
    public boolean eWorldBoss;
    public boolean eRaid;
    public ScreenResolutionProfile screenResolutionProfile;
    public String cfgProfileName;
    public ArrayList<Familiar> familiarToBribeWithGems;

    @SuppressWarnings("rawtypes")
    public ParseArgumentsResult(Class<? extends AbstractApplication> applicationClass, String[] arguments, ArrayList<FlagPattern> usingFlags) {
        this.applicationClass = applicationClass;
        this.arguments = arguments;
        this.usingFlags = usingFlags;
    }
}
