package bh.bot.app;

import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppMeta(code = "rerun", name = "ReRun", displayOrder = 2)
public class ReRunApp extends AbstractApplication {

    private int longTimeNoSee = Configuration.Timeout.defaultLongTimeNoSeeInMinutes * 60_000;

    @Override
    protected void internalRun(String[] args) {
        longTimeNoSee = Configuration.Timeout.longTimeNoSeeInMinutes * 60_000;
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
                () -> detectDefeatedOnRaid(masterSwitch),
                () -> autoReactiveAuto(masterSwitch),
                () -> autoExit(argumentInfo.exitAfterXSecs, masterSwitch),
                () -> doCheckGameScreenOffset(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoopClickImage(int loopCount, AtomicBoolean masterSwitch) {
        info("Starting ReRun");
        try {
            moveCursor(new Point(950, 100));
            long lastFound = System.currentTimeMillis();
            boolean clickedOnPreviousRound = false;
            while (loopCount > 0 && !masterSwitch.get()) {
                sleep(10_000);
                if (clickImage(BwMatrixMeta.Metas.Dungeons.Buttons.rerun)) {
                    loopCount--;
                    lastFound = System.currentTimeMillis();
                    info("%d loop left", loopCount);
                    clickedOnPreviousRound = true;
                } else {
                    if (System.currentTimeMillis() - lastFound > longTimeNoSee) {
                        info("Long time no see => Stop");
                        Telegram.sendMessage("long time no see button", true);
                        break;
                    } else {
                        debug("Not found, repeat");
                    }

                    if (clickedOnPreviousRound) {
                        clickedOnPreviousRound = false;
                        sleep(60_000);
                    }
                }
            }

            masterSwitch.set(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    private void detectDefeatedOnRaid(AtomicBoolean masterSwitch) {
        try {
            int sleepSecs = 60;
            int cnt = sleepSecs;
            while (!masterSwitch.get()) {
                cnt--;
                sleep(1000);
                if (cnt > 0) {
                    continue;
                }

                cnt = sleepSecs;
                if (clickImage(BwMatrixMeta.Metas.Raid.Buttons.town)) {
                    masterSwitch.set(true);
                    Telegram.sendMessage("Defeated", true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
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
    protected String getLimitationExplain() {
        return "This function only supports clicking the ReRun button, that means you have to enter Dungeon/Raid manually, turn on the Auto and when the ReRun button appears, it will be automatically clicked";
    }
}
