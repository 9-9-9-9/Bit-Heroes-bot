package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Telegram;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

public abstract class AbstractDoFarmingApp extends AbstractApplication {
    protected abstract String getAppShortName();

    protected abstract AttendablePlace getAttendablePlace();

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

            if (!readMoreInput(br)) return;
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
                () -> autoReactiveAuto(masterSwitch),
                () -> autoExit(argumentInfo.exitAfterXSecs, masterSwitch),
                () -> doCheckSteamWindow(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    protected boolean readMoreInput(BufferedReader br) throws IOException {
        return true;
    }

    protected void loop(int loopCount, AtomicBoolean masterSwitch) {
        info("Starting %s", getAppShortName());
        List<NextAction> internalPredefinedImageActions = getInternalPredefinedImageActions();
        int continuousNotFound = 0;
        final Point coordinateHideMouse = new Point(0, 0);
        ML:
        while (!masterSwitch.get() && loopCount > 0) {
            sleep(5_000);

            if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                debug("confirmStartNotFullTeam");
                InteractionUtil.Keyboard.sendSpaceKey();
                continuousNotFound = 0;
                moveCursor(coordinateHideMouse);
                continue ML;
            }

            if (doCustomAction()) {
                debug("doCustomAction");
                continuousNotFound = 0;
                moveCursor(coordinateHideMouse);
                continue ML;
            }

            for (NextAction predefinedImageAction : internalPredefinedImageActions) {
                if (clickImage(predefinedImageAction.image)) {
                    debug(predefinedImageAction.image.getImageNameCode());
                    continuousNotFound = 0;
                    if (predefinedImageAction.reduceLoopCountOnFound) {
                        loopCount--;
                        info("%d loop left", loopCount);
                    }
                    if (predefinedImageAction.isOutOfTurns) {
                        InteractionUtil.Keyboard.sendEscape();
                        masterSwitch.set(true);
                    }
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }
            }

            Point coordMap = findImage(BwMatrixMeta.Metas.Globally.Buttons.mapButtonOnFamiliarUi);
            if (coordMap != null) {
            	BwMatrixMeta.Metas.Globally.Buttons.mapButtonOnFamiliarUi.setLastMatchPoint(coordMap.x, coordMap.y);
                debug("mapButtonOnFamiliarUi");
                InteractionUtil.Keyboard.sendEscape();
                continuousNotFound = 0;
                moveCursor(coordinateHideMouse);
                continue ML;
            }

            debug("None");
            continuousNotFound++;
            moveCursor(coordinateHideMouse);

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

        masterSwitch.set(true);
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

    protected boolean doCustomAction() {
        return false;
    }

    protected abstract java.util.List<NextAction> getInternalPredefinedImageActions();

    public static class NextAction {
        public final BwMatrixMeta image;
        public final boolean reduceLoopCountOnFound;
        public final boolean isOutOfTurns;

        public NextAction(BwMatrixMeta image, boolean reduceLoopCountOnFound, boolean isOutOfTurns) {
            this.image = image;
            this.reduceLoopCountOnFound = reduceLoopCountOnFound;
            this.isOutOfTurns = isOutOfTurns;
        }
    }
}
