package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppMeta(code = "rerun", name = "ReRun", displayOrder = 2, argType = "number", argAsk = "How many times do you want to click the ReRun button?", argDefault = "100", argRequired = true)
@RequireSingleInstance
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
            arg = readInputLoopCount("How many times do you want to click ReRun buttons?");
        }

        final int loop = arg;
        info("%3d remaining loop left", loop);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoopClickImage(loop, masterSwitch),
				() -> internalDoSmallTasks( //
						masterSwitch, //
						SmallTasks //
								.builder() //
								.clickTalk() //
								.clickDisconnect() //
								.reactiveAuto() //
								.autoExit() //
                                .persuade() //
								.build() //
				), //
                () -> detectDefeatedOnRaid(masterSwitch),
                () -> doCheckGameScreenOffset(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoopClickImage(int loopCount, AtomicBoolean masterSwitch) {
        info(ColorizeUtil.formatInfo, "\n\nStarting ReRun");
        Main.warningSupport();
        try {
            final int mainLoopInterval = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());

            moveCursor(new Point(950, 100));
            long lastFound = System.currentTimeMillis();
            boolean clickedOnPreviousRound = false;
            while (loopCount > 0 && !masterSwitch.get()) {
                sleep(mainLoopInterval);
                if (clickImage(BwMatrixMeta.Metas.Dungeons.Buttons.rerun)) {
                    loopCount--;
                    long now = System.currentTimeMillis();
                    info("%d loop left (last round: %ds)", loopCount, (now - lastFound) / 1000);
                    lastFound = now;
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
                if (findImage(BwMatrixMeta.Metas.Dungeons.Buttons.rerun) != null) {
                    continue;
                }
                if (clickImage(BwMatrixMeta.Metas.Raid.Buttons.town)) {
                    warn("Detected 'Defeated' state");
                    Telegram.sendMessage("Defeated in Raid", true);
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

    @Override
    protected int getDefaultMainLoopInterval() {
        return 10_000;
    }
}
