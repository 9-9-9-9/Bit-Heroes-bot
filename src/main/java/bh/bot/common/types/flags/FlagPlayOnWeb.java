package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(cbDesc = "Specific that you want to use this bot to control BitHeroes on web", displayOrder = 1.1)
public class FlagPlayOnWeb extends FlagResolution {
    @Override
    public String getName() {
        return "web";
    }

    @Override
    public String getDescription() {
        return "Specific that you want to use this bot to control BitHeroes on web";
    }
}
