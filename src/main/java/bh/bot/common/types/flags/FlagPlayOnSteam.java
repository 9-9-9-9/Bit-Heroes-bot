package bh.bot.common.types.flags;

import bh.bot.common.types.Platform;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(cbDesc = "Specific that you want to use this bot to control BitHeroes on Steam", checked = true, displayOrder = 1)
public class FlagPlayOnSteam extends FlagResolution {
    @Override
    public String getName() {
        return "steam";
    }

    @Override
    public String getDescription() {
        return "Specific that you want to use this bot to control BitHeroes on Steam";
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Windows };
    }
}