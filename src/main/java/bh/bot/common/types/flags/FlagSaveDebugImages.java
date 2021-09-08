package bh.bot.common.types.flags;

public class FlagSaveDebugImages extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "img";
    }

    @Override
    public String getDescription() {
        return "Save debug images to 'out' folder";
    }

    @Override
    public boolean isDevelopersOnly() {
        return true;
    }

    @Override
    public boolean isGlobalFlag() {
        return true;
    }
}
