package bh.bot.common.types.flags;

import bh.bot.common.types.Platform;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(cbDesc = "Using Steam with game resolution 800x480", checked = true, displayOrder = 1)
@Deprecated
public class FlagSteamResolution800x480 extends FlagResolution {
    @Override
    public String getName() {
        return "steam";
    }

    @Override
    public String getDescription() {
        return "(Deprecated from version 2.0.0 due to game window will be resized to 800x520 resolution) When game resolution 800x480 while playing on Steam client";
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Windows };
    }
}