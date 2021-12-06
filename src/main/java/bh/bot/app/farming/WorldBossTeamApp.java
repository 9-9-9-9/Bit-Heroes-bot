package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
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

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppMeta(code = "world-boss-team", name = "World Boss (Team)", displayOrder = 6.1)
@RequireSingleInstance
public class WorldBossTeamApp extends AbstractApplication {
    // TODO check usage
    private int longTimeNoSee = Configuration.Timeout.defaultLongTimeNoSeeInMinutes * 60_000;

    @Override
    protected void internalRun(String[] args) {
        longTimeNoSee = Configuration.Timeout.longTimeNoSeeInMinutes * 60_000;
        int arg;
        try {
            arg = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            info(getHelp());
            arg = readInputLoopCount("How many times do you want to attack the world bosses?");
        }

        final int loop = arg;
        Log.info("Loop: %d", loop);
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
                                .build() //
                ), //
                () -> doCheckGameScreenOffset(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoopClickImage(int loopCount, AtomicBoolean masterSwitch) {
        info(ColorizeUtil.formatInfo, "\n\nStarting World Boss (Team)");
        info(ColorizeUtil.formatError, "*** NOTICE: REMEMBER YOU HAVE TO WATCH/CHECK THE GAME SOMETIME TO PREVENT UN-EXPECTED HANG/LOSS DUE TO UN-MANAGED BEHAVIORS LIKE MISSING MEMBERS,...ETC ***");
        try {
            final int mainLoopInterval = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());

            moveCursor(new Point(950, 100));
            long lastRound = System.currentTimeMillis();
            while (loopCount > 0 && !masterSwitch.get()) {
                sleep(mainLoopInterval);
                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroup)) {
                    loopCount--;
                    lastRound = System.currentTimeMillis();
                    info("%d loop left", loopCount);
                    continue;
                }

                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroupOnDefeated)) {
                    loopCount--;
                    lastRound = System.currentTimeMillis();
                    info("%d loop left (you was defeated this round)", loopCount);
                    continue;
                }

                if (System.currentTimeMillis() - lastRound > longTimeNoSee) {
                    info("Long time no see => Stop");
                    Telegram.sendMessage("long time no see button", true);
                    break;
                }

                if (findImage(BwMatrixMeta.Metas.WorldBoss.Buttons.unready) != null) {
                    debug("Team member (already ready) => waiting");
                    sleep(4_000);
                    continue;
                }

                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.ready)) {
                    debug("Team member => waiting");
                    continue;
                }

                if (findImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss) == null) {
                    debug("Team leader but not all members are ready => waiting");
                    continue;
                }


            }

            masterSwitch.set(true);
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
        return "Do World Boss in team";
    }

    @Override
    protected String getLimitationExplain() {
        return "World Boss is for team only (need more than 1 person) thus it's a bit complicated and is recommended to watch the bot in case bot is malfunctioned";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 1_800;
    }
}
