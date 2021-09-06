package bh.bot.app;

import bh.bot.Main;
import bh.bot.app.farming.*;
import bh.bot.app.farming.ExpeditionApp.ExpeditionPlace;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.Offset;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.*;
import static bh.bot.common.types.AttendablePlace.MenuItem;
import static bh.bot.common.utils.InteractionUtil.Keyboard.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppMeta(code = "afk", name = "AFK", displayOrder = 1)
public class AfkApp extends AbstractApplication {
    private InteractionUtil.Screen.Game gameScreenInteractor;
    private final AtomicLong blockPvpUntil = new AtomicLong(0);
    private final AtomicLong blockWorldBossUntil = new AtomicLong(0);
    private final AtomicLong blockRaidUntil = new AtomicLong(0);
    private final AtomicLong blockGvgAndInvasionAndExpeditionUntil = new AtomicLong(0);
    private final AtomicLong blockTrialsAndGauntletUntil = new AtomicLong(0);
    private ExpeditionPlace place = ExpeditionPlace.Astamus;

    @Override
    protected void internalRun(String[] args) {
        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        ArrayList<AttendablePlace> eventList;
        UserConfig userConfig = null;

        try {
            eventList = getAttendablePlaces();

            boolean doRaid = eventList.contains(AttendablePlaces.raid);
            boolean doWorldBoss = eventList.contains(AttendablePlaces.worldBoss);
            boolean doExpedition = eventList.contains(AttendablePlaces.expedition);
            if (doRaid || doWorldBoss) {
                int profileNumber = this.argumentInfo.profileNumber;
                if (profileNumber < 1)
                    profileNumber = readProfileNumber("You want to do Raid/World Boss so you have to specific profile number first!\nSelect profile number");
                Tuple2<Boolean, UserConfig> resultLoadUserConfig = Configuration
                        .loadUserConfig(profileNumber);
                if (!resultLoadUserConfig._1) {
                    err("Profile number %d could not be found", profileNumber);
                    printRequiresSetting();
                    System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
                }

                userConfig = resultLoadUserConfig._2;

                try {
                    if (doRaid && doWorldBoss) {
                        info(ColorizeUtil.formatInfo, "You have selected %s mode of %s", userConfig.getRaidModeDesc(),
                                userConfig.getRaidLevelDesc());
                        info(ColorizeUtil.formatInfo, "and World Boss %s", userConfig.getWorldBossLevelDesc());
                        warn("This function is solo only and does not support select mode of World Boss (Normal/Hard/Heroic), only select by default So which boss do you want to hit? Choose it before turn this on");
                    } else if (doRaid) {
                        info(ColorizeUtil.formatInfo, "You have selected %s mode of %s", userConfig.getRaidModeDesc(),
                                userConfig.getRaidLevelDesc());
                    } else //noinspection ConstantConditions
                        if (doWorldBoss) {
                            info(ColorizeUtil.formatInfo, "You have selected world boss level %s", userConfig.getWorldBossLevelDesc());
                            warn("This function is solo only and does not support select mode of World Boss (Normal/Hard/Heroic), only select by default So which boss do you want to hit? Choose it before turn this on");
                        }
                } catch (InvalidDataException ex2) {
                    err(ex2.getMessage());
                    printRequiresSetting();
                    System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
                    return;
                }
            }

            if (doExpedition)
                this.place = selectExpeditionPlace();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            return;
        }
        //
        final AtomicBoolean masterSwitch = new AtomicBoolean(false);
        final UserConfig finalUserConfig = userConfig;
        ThreadUtil.waitDone(() -> doLoop(masterSwitch, finalUserConfig, //
                eventList.contains(AttendablePlaces.pvp), //
                eventList.contains(AttendablePlaces.worldBoss), //
                eventList.contains(AttendablePlaces.raid), //
                eventList.contains(AttendablePlaces.gvg), //
                eventList.contains(AttendablePlaces.invasion), //
                eventList.contains(AttendablePlaces.expedition), //
                eventList.contains(AttendablePlaces.trials), //
                eventList.contains(AttendablePlaces.gauntlet) //
                ), () -> doClickTalk(masterSwitch::get), () -> detectDisconnected(masterSwitch),
                () -> autoReactiveAuto(masterSwitch), () -> autoExit(argumentInfo.exitAfterXSecs, masterSwitch),
                () -> doCheckGameScreenOffset(masterSwitch));
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoop(//
                        AtomicBoolean masterSwitch, UserConfig userConfig, //
                        boolean doPvp, //
                        boolean doWorldBoss, //
                        boolean doRaid, //
                        boolean doGvg, //
                        boolean doInvasion, //
                        boolean doExpedition, //
                        boolean doTrials, //
                        boolean doGauntlet //
    ) {
        try {

            if (doInvasion && Configuration.isSteamProfile)
                throw new NotSupportedException("Invasion has not been supported on Steam mode");

            info(ColorizeUtil.formatInfo, "\n\nStarting AFK");
            boolean isUnknownGvgOrInvasionOrExpedition = (doGvg && doInvasion) || (doGvg && doExpedition)
                    || (doInvasion && doExpedition);
            boolean isUnknownTrialsOrGauntlet = doTrials && doGauntlet;
            int continuousNotFound = 0;
            final Point coordinateHideMouse = new Point(0, 0);
            final ArrayList<Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>>> taskList = new ArrayList<>();
            if (doPvp)
                taskList.add(new Tuple3<>(AttendablePlaces.pvp, blockPvpUntil, PvpApp.getPredefinedImageActions()));
            if (doWorldBoss)
                taskList.add(new Tuple3<>(AttendablePlaces.worldBoss, blockWorldBossUntil,
                        WorldBossApp.getPredefinedImageActions()));
            if (doGvg)
                taskList.add(new Tuple3<>(AttendablePlaces.gvg, blockGvgAndInvasionAndExpeditionUntil,
                        GvgApp.getPredefinedImageActions()));
            if (doInvasion)
                taskList.add(new Tuple3<>(AttendablePlaces.invasion, blockGvgAndInvasionAndExpeditionUntil,
                        InvasionApp.getPredefinedImageActions()));
            if (doExpedition)
                taskList.add(new Tuple3<>(AttendablePlaces.expedition, blockGvgAndInvasionAndExpeditionUntil,
                        ExpeditionApp.getPredefinedImageActions()));
            if (doTrials)
                taskList.add(new Tuple3<>(AttendablePlaces.trials, blockTrialsAndGauntletUntil,
                        TrialsApp.getPredefinedImageActions()));
            if (doGauntlet)
                taskList.add(new Tuple3<>(AttendablePlaces.gauntlet, blockTrialsAndGauntletUntil,
                        GauntletApp.getPredefinedImageActions()));
            if (doRaid)
                taskList.add(new Tuple3<>(AttendablePlaces.raid, blockRaidUntil, getPredefinedImageActionsOfRaid()));

            for (Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>> tp : taskList) {
                for (AbstractDoFarmingApp.NextAction na : tp._3) {
                    if (na.image == null)
                        throw new InvalidDataException("Null occurs at %s (reduceLoopCountOnFound %s, isOutOfTurns %s)",
                                tp._1.name, String.valueOf(na.reduceLoopCountOnFound), String.valueOf(na.isOutOfTurns));
                }
            }

            ArrayList<AbstractDoFarmingApp.NextAction> outOfTurnNextActionList = new ArrayList<>();
            addOutOfTurnActionsToList(outOfTurnNextActionList, PvpApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, WorldBossApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, getPredefinedImageActionsOfRaid());
            addOutOfTurnActionsToList(outOfTurnNextActionList, GvgApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, InvasionApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, ExpeditionApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, TrialsApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, GauntletApp.getPredefinedImageActions());

            final ArrayList<AttendablePlace> toBeRemoved = new ArrayList<>();

            final byte minutesSleepWaitingResourceGeneration = 5;
            final short loopSleep = 5_000;
            final short originalCheckAreYouStillThereAfter = 20_000 / loopSleep;
            short checkAreYouStillThereAfter = originalCheckAreYouStillThereAfter;
            final short originalSleepWhileWaitingResourceRegen = 5 * 60_000 / loopSleep;
            short sleepWhileWaitingResourceRegen = 0;

            final Supplier<Boolean> isWorldBossBlocked = () -> !isNotBlocked(blockWorldBossUntil);

            ML:
            while (!masterSwitch.get()) {
                sleep(loopSleep);

                debug("doLoop on loop");
                if (toBeRemoved.size() > 0) {
                    if (taskList.removeIf(x -> toBeRemoved.contains(x._1))) {
                        toBeRemoved.clear();
                        continue ML;
                    }
                }

                if (Configuration.isSteamProfile) {
                    if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.areYouSureWantToExit)) {
                        info("areYouSureWantToExit");
                        sendEscape();
                        continue ML;
                    }
                }

                if (--checkAreYouStillThereAfter <= 0) {
                    if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.areYouStillThere)) {
                        info("Knock knock, are you still there?");
                        sendEnter();
                        sleep(1_000);
                        sendEscape();
                        checkAreYouStillThereAfter = 2;
                    } else {
                        checkAreYouStillThereAfter = originalCheckAreYouStillThereAfter;
                    }
                    continue ML;
                }

                if (sleepWhileWaitingResourceRegen > 0) {
                    sleepWhileWaitingResourceRegen--;
                    debug("sleepWhileWaitingResourceRegen--");
                    continue ML;
                }

                if (taskList.stream().noneMatch(x -> isNotBlocked(x._2))) {
                    info("Waiting for resource generation, sleeping %d minutes", minutesSleepWaitingResourceGeneration);
                    sleepWhileWaitingResourceRegen = originalSleepWhileWaitingResourceRegen;
                    continue ML;
                }

                if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                    debug("confirmStartNotFullTeam");
                    sendSpaceKey();
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmQuitBattle)) {
                    debug("confirmQuitBattle");
                    sendEnter();
                    sleep(1_000);
                    spamEscape(1);
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                Point coordMap = findImage(BwMatrixMeta.Metas.Globally.Buttons.mapButtonOnFamiliarUi);
                if (coordMap != null) {
                    BwMatrixMeta.Metas.Globally.Buttons.mapButtonOnFamiliarUi.setLastMatchPoint(coordMap.x, coordMap.y);
                    debug("mapButtonOnFamiliarUi");
                    sendEscape();
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                if (tryEnterRaid(doRaid, userConfig)) {
                    debug("tryEnterRaid");
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                if (tryEnterWorldBoss(doWorldBoss, userConfig, isWorldBossBlocked)) {
                    debug("tryEnterWorldBoss");
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                if (tryEnterExpedition(doExpedition, this.place)) {
                    debug("tryEnterExpedition");
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                for (Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>> tuple : taskList) {
                    if (!isNotBlocked(tuple._2))
                        continue;
                    AbstractDoFarmingApp.NextAction nextAction = tryToClickOnBatch(tuple._3);
                    if (nextAction == null)
                        continue;
                    debug(nextAction.image.getImageNameCode());
                    if (nextAction.isOutOfTurns) {
                        spamEscape(2);
                        tempBlock(tuple._1);
                    }
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                debug("None");
                continuousNotFound++;
                moveCursor(coordinateHideMouse);

                if (continuousNotFound >= 6) {
                    for (AbstractDoFarmingApp.NextAction nextAction : outOfTurnNextActionList) {
                        if (clickImage(nextAction.image)) {
                            spamEscape(2);
                            sleep(1_000);
                        }
                    }

                    for (Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>> tuple : taskList) {
                        if (!isNotBlocked(tuple._2))
                            continue;
                        debug("Finding %s icon", tuple._1.name);
                        Point point = this.gameScreenInteractor.findAttendablePlace(tuple._1);
                        if (point != null) {
                            if (isUnknownGvgOrInvasionOrExpedition) {
                                if (tuple._1 == AttendablePlaces.gvg) {
                                    isUnknownGvgOrInvasionOrExpedition = false;
                                    toBeRemoved.add(AttendablePlaces.invasion);
                                    toBeRemoved.add(AttendablePlaces.expedition);
                                }
                                if (tuple._1 == AttendablePlaces.invasion) {
                                    isUnknownGvgOrInvasionOrExpedition = false;
                                    toBeRemoved.add(AttendablePlaces.gvg);
                                    toBeRemoved.add(AttendablePlaces.expedition);
                                }
                                if (tuple._1 == AttendablePlaces.expedition) {
                                    isUnknownGvgOrInvasionOrExpedition = false;
                                    toBeRemoved.add(AttendablePlaces.gvg);
                                    toBeRemoved.add(AttendablePlaces.invasion);
                                }
                            }
                            if (isUnknownTrialsOrGauntlet) {
                                if (tuple._1 == AttendablePlaces.trials) {
                                    isUnknownTrialsOrGauntlet = false;
                                    toBeRemoved.add(AttendablePlaces.gauntlet);
                                }
                                if (tuple._1 == AttendablePlaces.gauntlet) {
                                    isUnknownTrialsOrGauntlet = false;
                                    toBeRemoved.add(AttendablePlaces.trials);
                                }
                            }

                            moveCursor(point);
                            mouseClick();
                            sleep(100);
                            moveCursor(coordinateHideMouse);
                            continuousNotFound = 0;
                            continue ML;
                        }
                    }
                }
            }
            debug("end of doLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
            err("doLoop encountered error!");
        } finally {
            masterSwitch.set(true);
        }
    }

    private void addOutOfTurnActionsToList(ArrayList<AbstractDoFarmingApp.NextAction> list,
                                           List<AbstractDoFarmingApp.NextAction> predefinedNextActions) {
        list.addAll(predefinedNextActions.stream().filter(x -> x.isOutOfTurns).collect(Collectors.toList()));
    }

    private AbstractDoFarmingApp.NextAction tryToClickOnBatch(
            List<AbstractDoFarmingApp.NextAction> predefinedImageActions) {
        for (AbstractDoFarmingApp.NextAction predefinedImageAction : predefinedImageActions)
            if (clickImage(predefinedImageAction.image))
                return predefinedImageAction;
        return null;
    }

    private boolean isNotBlocked(AtomicLong blockUntil) {
        return blockUntil.get() < System.currentTimeMillis();
    }

    private void tempBlock(AttendablePlace attendablePlace) {
        AtomicLong x;
        if (attendablePlace == AttendablePlaces.pvp)
            x = blockPvpUntil;
        else if (attendablePlace == AttendablePlaces.worldBoss)
            x = blockWorldBossUntil;
        else if (attendablePlace == AttendablePlaces.raid)
            x = blockRaidUntil;
        else if (attendablePlace == AttendablePlaces.gvg || attendablePlace == AttendablePlaces.invasion
                || attendablePlace == AttendablePlaces.expedition)
            x = blockGvgAndInvasionAndExpeditionUntil;
        else if (attendablePlace == AttendablePlaces.trials || attendablePlace == AttendablePlaces.gauntlet)
            x = blockTrialsAndGauntletUntil;
        else
            throw new NotSupportedException(String.format("Not supported AttendablePlace.%s", attendablePlace.name));
        x.set(System.currentTimeMillis() + attendablePlace.procedureTicketMinutes * 60_000);
    }

    private ArrayList<AttendablePlace> getAttendablePlaces() {
        ArrayList<AttendablePlace> eventList = new ArrayList<>();
        final List<AttendablePlace> allAttendablePlaces = Arrays.asList(//
                AttendablePlaces.invasion, //
                AttendablePlaces.expedition, //
                AttendablePlaces.trials, //
                AttendablePlaces.gvg, //
                AttendablePlaces.gauntlet, //

                AttendablePlaces.pvp, //
                AttendablePlaces.worldBoss, //
                AttendablePlaces.raid //
        );
        if (argumentInfo.hasFlagAll)
            eventList.addAll(allAttendablePlaces);
        //
        if (argumentInfo.eInvasion)
            eventList.add(AttendablePlaces.invasion);
        if (argumentInfo.eExpedition)
            eventList.add(AttendablePlaces.expedition);
        if (argumentInfo.eGvg)
            eventList.add(AttendablePlaces.gvg);
        if (argumentInfo.eTrials)
            eventList.add(AttendablePlaces.trials);
        if (argumentInfo.eGauntlet)
            eventList.add(AttendablePlaces.gauntlet);

        if (argumentInfo.ePvp)
            eventList.add(AttendablePlaces.pvp);
        if (argumentInfo.eWorldBoss)
            eventList.add(AttendablePlaces.worldBoss);
        if (argumentInfo.eRaid)
            eventList.add(AttendablePlaces.raid);
        //
        if (eventList.size() == 0) {

            final List<MenuItem> menuItems = Stream
                    .concat(allAttendablePlaces.stream().map(MenuItem::from), Stream.of(
                            MenuItem.from(AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.expedition, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.gvg, AttendablePlaces.gauntlet),
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.raid),
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.raid,
                                    AttendablePlaces.expedition, AttendablePlaces.trials)))
                    .collect(Collectors.toList());

            String menuItem = menuItems.stream().map(x -> String.format("  %3d. %s", x.num, x.name))
                    .collect(Collectors.joining("\n"));

            final ArrayList<AttendablePlace> selectedOptions = new ArrayList<>();
            final Supplier<List<String>> selectedOptionsInfoProvider = () -> selectedOptions.stream().map(x -> x.name)
                    .collect(Collectors.toList());

            String ask = "Select events you want to do:\n" + menuItem;
            while (true) {
                List<AttendablePlace> events = readInput(ask,
                        "To select an event, press the number then press Enter. To finish input, just enter without supply a number",
                        selectedOptionsInfoProvider,
                        s -> {
                            try {
                                int result = Integer.parseInt(s);
                                List<AttendablePlace> events1 = allAttendablePlaces.stream()
                                        .filter(x -> (result & x.id) == x.id).collect(Collectors.toList());
                                if (events1.size() == 0)
                                    return new Tuple3<>(false, "Incorrect value", null);
                                return new Tuple3<>(true, null, events1);
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse your input, error: " + ex2.getMessage(),
                                        null);
                            }
                        }, true);

                if (events == null)
                    break;
                eventList.addAll(events);
                eventList = eventList.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
                selectedOptions.clear();
                selectedOptions.addAll(eventList);
            }

            if (eventList.size() == 0) {
                info("None option was selected, exit now");
                System.exit(0);
            }
        }

        eventList = eventList.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        info("Selected:");
        for (AttendablePlace event : eventList) {
            info("  <%3d> %s", event.id, event.name);
        }

        return eventList;
    }

    private boolean tryEnterRaid(boolean doRaid, UserConfig userConfig) {
        Point coord = findImage(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog);
        if (coord == null) {
            debug("Label raid not found");
            return false;
        }
        if (!isNotBlocked(blockRaidUntil) || !doRaid) {
            spamEscape(1);
            return false;
        }
        mouseMoveAndClickAndHide(coord);
        BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.setLastMatchPoint(coord.x, coord.y);
        Tuple2<Point[], Byte> result = detectRadioButtons(
                Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
        Point[] points = result._1;
        int selectedLevel = result._2 + 1;
        info("Found %d, selected %d", points.length, selectedLevel);
        if (selectedLevel != userConfig.raidLevel)
            clickRadioButton(userConfig.raidLevel, points, "Raid");
        sleep(3_000);
        result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
        selectedLevel = result._2 + 1;
        if (selectedLevel != userConfig.raidLevel) {
            err("Failure on selecting raid level");
            spamEscape(1);
            return false;
        }
        sleep(1_000);
        mouseMoveAndClickAndHide(
                fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog, coord,
                        Configuration.screenResolutionProfile.getOffsetButtonSummonOfRaid()));
        sleep(5_000);
        if (UserConfig.isNormalMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterNormalRaid()));
        } else if (UserConfig.isHardMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterHardRaid()));
        } else if (UserConfig.isHeroicMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterHeroicRaid()));
        } else {
            throw new InvalidDataException("Unknown raid mode value: %d", userConfig.raidMode);
        }
        return true;
    }

    private Point fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta sampleImg, Point sampleImgCoord,
                                                              Offset targetOffset) {
        int x = sampleImgCoord.x - sampleImg.getCoordinateOffset().X;
        int y = sampleImgCoord.y - sampleImg.getCoordinateOffset().Y;
        return new Point(x + targetOffset.X, y + targetOffset.Y);
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "It helps you AFK by automatically consume PVP/World Boss/GVG/Invasion/Trials/Gauntlet turns";
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select level/mode, how many badge/ticket/... to consumes and can only everything by default so please chose everything first manually then use this";
    }

    private List<AbstractDoFarmingApp.NextAction> getPredefinedImageActionsOfRaid() {
        return Arrays.asList(new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Buttons.town, true, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.rerun, true, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Buttons.accept, false, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Dialogs.notEnoughShards, false, true));
    }
}
