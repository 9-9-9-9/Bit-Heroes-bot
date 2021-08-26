package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Telegram;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
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
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

public abstract class AbstractDoFarmingApp extends AbstractApplication {
    protected abstract String getAppShortName();
    protected abstract AttendablePlace getAttendablePlace();
    protected abstract Tuple2<Boolean, Boolean> isClickedSomething();
    protected abstract boolean isOutOfTicket();

    protected final AttendablePlace ap = getAttendablePlace();
    protected InteractionUtil.Screen.Game gameScreenInteractor;

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
                loopCount = readInput(br, "Loop count:", "How many time do you want to do " + getAppShortName(), new Function<String, Tuple3<Boolean, String, Integer>>() {
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

    protected void loop(int loopCount, AtomicBoolean masterSwitch) {
        int continuousNotFound = 0;
        final Point coordinateHideMouse = new Point(0, 0);
        while (!masterSwitch.get() && loopCount > 0) {
            sleep(5_000);

            Tuple2<Boolean, Boolean> result = isClickedSomething();
            boolean clickedSomething = result._1;
            if (clickedSomething) {
                debug("isClickedSomething");
                continuousNotFound = 0;

                boolean decreaseLoopCount = result._2;
                if (decreaseLoopCount) {
                    loopCount--;
                    info("%d loop left", loopCount);
                }

                moveCursor(coordinateHideMouse);

                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                debug("confirmStartNotFullTeam");
                InteractionUtil.Keyboard.sendSpaceKey();
                continuousNotFound = 0;

                moveCursor(coordinateHideMouse);

                continue;
            }

            if (isOutOfTicket()) {
                debug("isOutOfTicket");
                InteractionUtil.Keyboard.sendEscape();
                masterSwitch.set(true);
                continuousNotFound = 0;

                moveCursor(coordinateHideMouse);

                continue;
            }

            debug("None");
            continuousNotFound++;

            if (continuousNotFound >= 12) {
                debug("Finding %s icon", getAppShortName());
                Point point = this.gameScreenInteractor.findAttendablePlace(ap);
                if (point != null) {
                    moveCursor(point);
                    mouseClick();
                    sleep(100);
                    moveCursor(coordinateHideMouse);
                }
                continuousNotFound = 0;
            }
        }
    }

    @Override
    protected String getAppName() {
        return "BH-" + getAppShortName();
    }

    @Override
    protected String getScriptFileName() {
        return getAppCode();
    }

    @Override
    protected String getUsage() {
        return "<count>";
    }

    @Override
    protected String getDescription() {
        return "Do " + getAppShortName();
    }

    @Override
    protected String getFlags() {
        return buildFlags(
                "--exit=X : exit after X seconds if turns not all consumed, can be used to make sure do not run after boost has expired"
        );
    }
}
