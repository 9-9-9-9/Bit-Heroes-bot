package bh.bot.common.types.flags;

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
