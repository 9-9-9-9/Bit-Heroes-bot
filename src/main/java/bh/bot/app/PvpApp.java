package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Telegram;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.ThreadUtil.sleep;

public class PvpApp extends AbstractApplication {
    private final AttendablePlace ap = AttendablePlaces.pvp;
    private InteractionUtil.Screen.Game gameScreenInteractor;

    @Override
    protected void internalRun(String[] args) {
        int loopCount;
        try (
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
        ) {
            try {
                loopCount = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                info(getHelp());
                loopCount = readInput(br, "Loop count:", "How many time do you want to do PVP", new Function<String, Tuple3<Boolean, String, Integer>>() {
                    @Override
                    public Tuple3<Boolean, String, Integer> apply(String s) {
                        try {
                            int result = Integer.parseInt(s, 16) & 0xFFFFFF;
                            return new Tuple3<>(true, null, result);
                        } catch (Exception ex2) {
                            return new Tuple3<>(false, "Unable to parse, error: " + ex.getMessage(), 0);
                        }
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            throw new RuntimeException(ex);
        }

        final int cnt = loopCount;

        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> loop(cnt, masterSwitch),
                () -> detectDisconnected(masterSwitch),
                () -> autoExit(launchInfo.exitAfterXSecs, masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void loop(int loopCount, AtomicBoolean masterSwitch) {
        int continuousNotFound = 0;
        while (!masterSwitch.get() && loopCount > 0) {
            sleep(5_000);

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.play)) {
                debug("play");
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.fight1)) {
                debug("fight1");
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.accept)) {
                debug("accept");
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.townOnWin)) {
                debug("townOnWin");
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                debug("confirmStartNotFullTeam");
                InteractionUtil.Keyboard.sendSpaceKey();
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Dialogs.notEnoughTicket)) {
                debug("notEnoughTicket");
                InteractionUtil.Keyboard.sendEscape();
                masterSwitch.set(true);
                continuousNotFound = 0;
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.townOnLose)) {
                debug("townOnLose");
                continuousNotFound = 0;
                continue;
            }

            debug("None");
            continuousNotFound++;

            if (continuousNotFound >= 12) {
                info("Finding PVP icon");
                Point point = this.gameScreenInteractor.findAttendablePlace(ap);
                if (point != null) {
                    InteractionUtil.Mouse.moveCursor(point);
                    InteractionUtil.Mouse.mouseClick();
                }
                continuousNotFound = 0;
            }
        }
    }

    @Override
    public String getAppCode() {
        return "pvp";
    }

    @Override
    protected String getAppName() {
        return "BH-PVP";
    }

    @Override
    protected String getScriptFileName() {
        return "pvp";
    }

    @Override
    protected String getUsage() {
        return "<count>";
    }

    @Override
    protected String getDescription() {
        return "Do PVP";
    }

    @Override
    protected String getFlags() {
        return buildFlags(
                "--exit=X : exit after X seconds if turns not all consumed, can be used to make sure do not run after boost has expired"
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function only hit first opponent and does not support select PVP ticket cost, so choose it before turn this on";
    }
}
