package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(cbDesc = "Display help for this function", displayOrder = 10)
public class FlagPrintHelpMessage extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Display help message of specific function";
    }

    @Override
    public boolean isGlobalFlag() {
        return true;
    }
}
