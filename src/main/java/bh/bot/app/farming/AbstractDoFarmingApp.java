package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

@RequireSingleInstance
public abstract class AbstractDoFarmingApp extends AbstractApplication {
    protected abstract AttendablePlace getAttendablePlace();

    protected final AttendablePlace ap = getAttendablePlace();
    protected InteractionUtil.Screen.Game gameScreenInteractor;
    protected UserConfig userConfig;

    @Override
    protected void internalRun(String[] args) {
        int loopCount;
        try {
            try {
                loopCount = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                info(getHelp());
                loopCount = readInput("Loop count:", "how many time do you want to do " + getAppName(), s -> {
                    try {
                        int result = Integer.parseInt(s);
                        if (result < 2) {
                            return new Tuple3<>(false, "Must be greater than 1", 0);
                        }
                        return new Tuple3<>(true, null, result);
                    } catch (Exception ex2) {
                        return new Tuple3<>(false, "Unable to parse, error: " + ex.getMessage(), 0);
                    }
                });
            }

            if (!readMoreInput()) return;
        } catch (IOException ex) {
            ex.printStackTrace();
            Main.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            throw new RuntimeException(ex);
        }

        final int cnt = loopCount;
        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone( //
                () -> loop(cnt, masterSwitch), //
				() -> internalDoSmallTasks( //
						masterSwitch, //
						SmallTasks //
								.builder() //
								.clickTalk() //
								.clickDisconnect() //
								.reactiveAuto() //
								.autoExit() //
								.closeEnterGameNewsDialog() //
                                .persuade() //
								.build() //
				), //
                () -> doCheckGameScreenOffset(masterSwitch) //
        );
        Telegram.sendMessage("Stopped", false);
    }

    protected boolean readMoreInput() throws IOException {
        return true;
    }

    protected void loop(int loopCount, AtomicBoolean masterSwitch) {
        try {

            info(ColorizeUtil.formatInfo, "\n\nStarting %s", getAppName());
            List<NextAction> internalPredefinedImageActions = getInternalPredefinedImageActions();
            NextAction naBtnFight = null;
            if (this instanceof PvpApp && userConfig != null && userConfig.isValidPvpTarget()) {
                final BwMatrixMeta fight1 = BwMatrixMeta.Metas.PvpArena.Buttons.fight1;
                Optional<NextAction> first = internalPredefinedImageActions.stream().filter(x -> x.image == fight1).findFirst();
                if (first.isPresent()) {
                    naBtnFight = first.get();
                    final NextAction tmp = naBtnFight;
                    if (naBtnFight != null) {
                        internalPredefinedImageActions = internalPredefinedImageActions.stream().filter(x -> x != tmp).collect(Collectors.toList());
                    }
                }
            }
            int continuousNotFound = 0;
            final Point coordinateHideMouse = new Point(0, 0);
            final int mainLoopInterval = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());
            final int selectFightPvp = naBtnFight != null && this instanceof PvpApp ? userConfig.pvpTarget : 0;
            final int offsetTargetPvp = selectFightPvp < 1 ? 0 : (selectFightPvp - 1) * Configuration.screenResolutionProfile.getOffsetDiffBetweenFightButtons();

            Main.warningSupport();

            long lastLoop = System.currentTimeMillis();

            ML:
            while (!masterSwitch.get() && loopCount > 0) {
                sleep(mainLoopInterval);

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
                            long now = System.currentTimeMillis();
                            info("%d loop left (last round: %ds)", loopCount, (now - lastLoop) / 1000);
                            lastLoop = now;
                        }
                        if (predefinedImageAction.isOutOfTurns) {
                            InteractionUtil.Keyboard.sendEscape();
                            masterSwitch.set(true);
                        }
                        moveCursor(coordinateHideMouse);
                        continue ML;
                    }
                }

                if (selectFightPvp > 0) {
                    Point p = findImage(naBtnFight.image);
                    if (p != null) {
                        int offset = Configuration.Features.isFunctionDisabled("target-pvp") ? 0 : offsetTargetPvp;
                        mouseMoveAndClickAndHide(new Point(p.x, p.y + offset));
                        if (naBtnFight.reduceLoopCountOnFound) {
                            loopCount--;
                            info("%d loop left", loopCount);
                        }
                        if (naBtnFight.isOutOfTurns) {
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

                if (continuousNotFound >= 6) {
                    debug("Finding %s icon", getAppName());
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
        return "Do " + getAppName();
    }

    protected boolean doCustomAction() {
        return false;
    }

    protected abstract java.util.List<NextAction> getInternalPredefinedImageActions();

    @Override
    protected int getDefaultMainLoopInterval() {
        return 5_000;
    }

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
