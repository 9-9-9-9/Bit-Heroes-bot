package bh.bot.common.types.flags;

@Deprecated
public class FlagWebResolution800x520 extends FlagResolution {
    @Override
    public String getName() {
        return "web";
    }

    @Override
    public String getDescription() {
        return "(Deprecated from 2.0.0 due to unite resolution to 800x520 thus no need to provide this flag) (default) When game resolution 800x520 while playing on website";
    }
}
