package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.FishingApp;
import bh.bot.app.ReRunApp;
import bh.bot.app.dev.ScreenCaptureApp;
import bh.bot.common.Configuration;
import bh.bot.common.types.Platform;

public class FlagSteamResolution800x480 extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "steam";
    }

    @Override
    public String getDescription() {
        return "Use mode game resolution 800x480 (Bit Heroes on Steam)";
    }

    @Override
    public boolean isSupportedOnCurrentOsPlatform() {
        return Configuration.OS.platform == Platform.Windows;
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Windows };
    }

    @Override
    public <TApp extends AbstractApplication> boolean isSupportedByApp(TApp instance) {
        return instance instanceof ReRunApp
                || instance instanceof FishingApp
                || instance instanceof ScreenCaptureApp;
    }
}
