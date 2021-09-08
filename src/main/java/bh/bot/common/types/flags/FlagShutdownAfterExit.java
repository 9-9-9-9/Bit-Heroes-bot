package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.FishingApp;
import bh.bot.app.ReRunApp;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.common.OS;
import bh.bot.common.types.Platform;

public class FlagShutdownAfterExit extends FlagPattern.NonParamFlag {
    public static final byte shutdownAfterXMinutes = 2;

    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public String getDescription() {
        return String.format(
                "Shutdown your computer within %d minutes after bot exited%s",
                shutdownAfterXMinutes,
                OS.isWin
                        ? ""
                        : ". Require bot ran as sudo in able to run shutdown command: 'sudo shutdown now' upon exit"
        );
    }

    @Override
    public Platform[] getSupportedOsPlatforms() {
        return new Platform[]{Platform.Windows, Platform.Linux};
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof ReRunApp
                || instance instanceof FishingApp
                || instance instanceof AbstractDoFarmingApp
                || instance instanceof AfkApp;
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }
}
