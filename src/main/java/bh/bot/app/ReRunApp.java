package bh.bot.app;

import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ThreadUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import static bh.bot.common.Log.info;

public class ReRunApp extends AbstractApplication {

    @Override
    protected void internalRun(String[] args) {
        int arg;
        try {
            arg = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            info(getHelp());
            arg = readInput("How many times do you want to click ReRun buttons?", "Numeric only", s -> {
                try {
                    int num = Integer.parseInt(s);
                    if (num < 1) {
                        return new Tuple3<>(false, "Must greater than 0", 0);
                    }
                    return new Tuple3<>(true, null, num);
                } catch (NumberFormatException ex1) {
                    return new Tuple3<>(false, "The value you inputted is not a number", 0);
                }
            });
        }

        final int loop = arg;
        Log.info("Loop: %d", loop);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoopClickImage(loop, masterSwitch),
                () -> doClickTalk(masterSwitch::get),
                () -> detectDisconnected(masterSwitch),
                () -> autoExit(exitAfterXSecs, masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    @Override
    public String getAppCode() {
        return "rerun";
    }

    @Override
    protected String getAppName() {
        return "BH-ReRun";
    }

    @Override
    protected String getScriptFileName() {
        return "rerun";
    }

    @Override
    protected String getUsage() {
        return "<count>";
    }

    @Override
    protected String getDescription() {
        return "Click ReRun button. Used to farming Dungeons and Raid";
    }

    @Override
    protected boolean clickImage() {
        return clickImage(BwMatrixMeta.Metas.Dungeons.Buttons.rerun);
    }

    @Override
    protected String getFlags() {
        return buildFlags(
                "--exit=X : exit after X seconds if turns not all consumed, can be used to make sure do not run after boost has expired"
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function only supports clicking the ReRun button, that means you have to enter Dungeon/Raid manually, turn on the Auto and when the ReRun button appears, it will be automatically clicked";
    }
}
