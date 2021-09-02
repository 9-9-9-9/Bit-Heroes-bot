package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.FishingApp;
import bh.bot.app.ReRunApp;
import bh.bot.app.dev.ScreenCaptureApp;
import bh.bot.app.dev.TestApp;
import bh.bot.app.farming.GauntletApp;
import bh.bot.app.farming.GvgApp;
import bh.bot.app.farming.PvpApp;
import bh.bot.app.farming.WorldBossApp;
import bh.bot.common.types.Platform;

public class FlagSteamResolution800x480 extends FlagResolution {
    @Override
    public String getName() {
        return "steam";
    }

    @Override
    public String getDescription() {
        return "When game resolution 800x480 while playing on Steam client";
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Windows };
    }

    @Override
    public <TApp extends AbstractApplication> boolean isSupportedByApp(TApp instance) {
        return instance instanceof ReRunApp
                || instance instanceof FishingApp
                || instance instanceof ScreenCaptureApp
                || instance instanceof TestApp

                || instance instanceof PvpApp
                || instance instanceof WorldBossApp
                || instance instanceof GvgApp
                || instance instanceof GauntletApp
                || instance instanceof AfkApp;
    }
}
