package bh.bot.common.types.flags;

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
}