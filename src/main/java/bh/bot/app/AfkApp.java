package bh.bot.app;

import bh.bot.Main;
import bh.bot.app.farming.*;
import bh.bot.app.farming.AbstractDoFarmingApp.NextAction;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.flags.FlagExitAfkAfterIfWaitResourceGeneration;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.StringUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseMoveAndClickAndHide;
import static bh.bot.common.utils.ThreadUtil.sleep;
import static bh.bot.common.utils.ThreadUtil.waitDone;

@AppMeta(code = "afk", name = "AFK", displayOrder = 1, argType = "afk", argAsk = "Task combination (optional)", argDefault = "pbr")
@RequireSingleInstance
public class AfkApp extends AbstractApplication {
    private InteractionUtil.Screen.Game gameScreenInteractor;
    private final AtomicLong blockPvpUntil = new AtomicLong(0);
    private final AtomicLong blockWorldBossUntil = new AtomicLong(0);
    private final AtomicLong blockRaidUntil = new AtomicLong(0);
    private final AtomicLong blockGvgAndInvasionAndExpeditionUntil = new AtomicLong(0);
    private final AtomicLong blockTrialsAndGauntletUntil = new AtomicLong(0);
    private final AtomicBoolean isOnPvp = new AtomicBoolean(false);
    private byte expeditionPlace = UserConfig.getExpeditionPlaceRange()._1;

    @Override
    protected void internalRun(String[] args) {
        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        ArrayList<AttendablePlace> eventList;
        UserConfig userConfig = null;

        try {
            eventList = getAttendablePlaces(getEventListFromArg(args));

            boolean doRaid = eventList.contains(AttendablePlaces.raid);
            boolean doWorldBoss = eventList.contains(AttendablePlaces.worldBoss);
            boolean doExpedition = eventList.contains(AttendablePlaces.expedition);
            boolean doPVP = eventList.contains(AttendablePlaces.pvp);
            if (doRaid || doWorldBoss || doExpedition || doPVP) {
                userConfig = getPredefinedUserConfigFromProfileName("You want to do Raid/World Boss/Expedition/PVP so you have to specific profile name first!\nSelect an existing profile:");

                try {
                    if (doRaid) {
                        info(ColorizeUtil.formatInfo, "You have selected %s mode of %s", userConfig.getRaidModeDesc(),
                                userConfig.getRaidLevelDesc());
                    }

                    if (doWorldBoss) {
                        info(ColorizeUtil.formatInfo, "You have selected world boss %s", userConfig.getWorldBossLevelDesc());
                        warn("World Boss is solo only and does not support select mode of World Boss (Normal/Hard/Heroic), only select by default So which boss do you want to hit? Choose it before turn this on");
                    }

                    if (doExpedition) {
                        try {
                            info(ColorizeUtil.formatInfo, "You have selected to farm %s of Expedition", userConfig.getExpeditionPlaceDesc());
                            expeditionPlace = userConfig.expeditionPlace;
                        } catch (InvalidDataException ex2) {
                            warn("You haven't specified an Expedition door to enter so you have to select manually");
                            expeditionPlace = selectExpeditionPlace();
                        }
                    }

                    if (doPVP)
                        info(ColorizeUtil.formatInfo, "You have chosen to select target on the %s of PVP", userConfig.getPvpTargetDesc());
                } catch (InvalidDataException ex2) {
                    err(ex2.getMessage());
                    printRequiresSetting();
                    Main.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
                    return;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Main.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            return;
        }
        //
        final AtomicBoolean masterSwitch = new AtomicBoolean(false);
        final UserConfig finalUserConfig = userConfig;
        waitDone( //
                () -> doLoop( //
                        masterSwitch, finalUserConfig, //
                        eventList.contains(AttendablePlaces.pvp), //
                        eventList.contains(AttendablePlaces.worldBoss), //
                        eventList.contains(AttendablePlaces.raid), //
                        eventList.contains(AttendablePlaces.gvg), //
                        eventList.contains(AttendablePlaces.invasion), //
                        eventList.contains(AttendablePlaces.expedition), //
                        eventList.contains(AttendablePlaces.trials), //
                        eventList.contains(AttendablePlaces.gauntlet) //
                ), //
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
            info(ColorizeUtil.formatInfo, "\n\nStarting AFK");
            boolean isUnknownGvgOrInvasionOrExpedition = (doGvg && doInvasion) || (doGvg && doExpedition)
                    || (doInvasion && doExpedition);
            boolean isUnknownTrialsOrGauntlet = doTrials && doGauntlet;
            int continuousNotFound = 0;
            final Point coordinateHideMouse = new Point(0, 0);
            final ArrayList<Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>>> taskList = new ArrayList<>();
            NextAction naBtnFightPvp = null;
            if (doPvp) {
                List<AbstractDoFarmingApp.NextAction> pvpPia = PvpApp.getPredefinedImageActions();
                if (userConfig.isValidPvpTarget()) {
                    final BwMatrixMeta fight1 = BwMatrixMeta.Metas.PvpArena.Buttons.fight1;
                    Optional<NextAction> first = pvpPia.stream().filter(x -> x.image == fight1).findFirst();
                    if (first.isPresent()) {
                        naBtnFightPvp = first.get();
                        final NextAction tmp = naBtnFightPvp;
                        if (naBtnFightPvp != null) {
                            pvpPia = pvpPia.stream().filter(x -> x != tmp).collect(Collectors.toList());
                        }
                    }
                }
                taskList.add(new Tuple3<>(AttendablePlaces.pvp, blockPvpUntil, pvpPia));
            }
            final int selectFightPvp = naBtnFightPvp != null ? userConfig.pvpTarget : 0;
            final int offsetTargetPvp = selectFightPvp < 1 ? 0 : (selectFightPvp - 1) * Configuration.screenResolutionProfile.getOffsetDiffBetweenFightButtons();

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
                taskList.add(new Tuple3<>(AttendablePlaces.raid, blockRaidUntil, RaidApp.getPredefinedImageActions()));

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
            addOutOfTurnActionsToList(outOfTurnNextActionList, RaidApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, GvgApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, InvasionApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, ExpeditionApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, TrialsApp.getPredefinedImageActions());
            addOutOfTurnActionsToList(outOfTurnNextActionList, GauntletApp.getPredefinedImageActions());

            final ArrayList<AttendablePlace> toBeRemoved = new ArrayList<>();

            final byte minutesSleepWaitingResourceGeneration = 5;
            final int loopSleep = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());
            final int originalCheckAreYouStillThereAfter = 20_000 / loopSleep;
            int checkAreYouStillThereAfter = originalCheckAreYouStillThereAfter;
            final int originalSleepWhileWaitingResourceRegen = 5 * 60_000 / loopSleep;
            int sleepWhileWaitingResourceRegen = 0;

            final Supplier<Boolean> isWorldBossBlocked = () -> !isNotBlocked(blockWorldBossUntil);
            final Supplier<Boolean> isRaidBlocked = () -> !isNotBlocked(blockRaidUntil);

            Main.warningEnergyRefill();

            if (doRaid)
                info(ColorizeUtil.formatInfo, "Raid: %s of %s", userConfig.getRaidModeDesc(), userConfig.getRaidLevelDesc());
            if (doWorldBoss)
                info(ColorizeUtil.formatInfo, "World Boss: %s", userConfig.getWorldBossLevelDesc());
            if (doExpedition) {
                info(ColorizeUtil.formatInfo, "Expedition: (%d) %s", this.expeditionPlace, UserConfig.getExpeditionPlaceDesc(this.expeditionPlace));
                printWarningExpeditionImplementation();
            }
            if (doPvp) {
                info(ColorizeUtil.formatInfo, "PVP target: %s", userConfig.getPvpTargetDesc());
                warningPvpTargetSelectionCase();
            }

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
                        debug("areYouSureWantToExit");
                        sendEscape();
                        continue ML;
                    }
                }

                if (--checkAreYouStillThereAfter <= 0) {
                    if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.areYouStillThere)) {
                        debug("Knock knock, are you still there?");
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
                    if (this.argumentInfo.exitAfkIfWaitForResourceGeneration) {
                        masterSwitch.set(true);
                        FlagExitAfkAfterIfWaitResourceGeneration flag = new FlagExitAfkAfterIfWaitResourceGeneration();
                        warn("Due to flag '%s', AFK will exit now", flag.getCode());
                    }
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

                if (tryEnterRaid(doRaid, userConfig, isRaidBlocked)) {
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

                if (tryEnterExpedition(doExpedition, this.expeditionPlace)) {
                    debug("tryEnterExpedition");
                    continuousNotFound = 0;
                    moveCursor(coordinateHideMouse);
                    continue ML;
                }

                for (Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>> tuple : taskList) {
                    if (!isNotBlocked(tuple._2))
                        continue;
                    AbstractDoFarmingApp.NextAction nextAction = tryToClickOnBatch(tuple._3);
                    if (nextAction == null) {
                        if (selectFightPvp > 0 && tuple._1 == AttendablePlaces.pvp) {
                            if (isOnPvp.get()) {
                                Point p = findImage(naBtnFightPvp.image);
                                if (p != null) {
                                    mouseMoveAndClickAndHide(new Point(p.x, p.y + offsetTargetPvp));
                                    moveCursor(coordinateHideMouse);
                                    continue ML;
                                }
                            } else if (clickImage(naBtnFightPvp.image)) {
                                continue ML;
                            }
                        }
                        continue;
                    }
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
                            isOnPvp.set(tuple._1 == AttendablePlaces.pvp);

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

    private ArrayList<AttendablePlace> getAttendablePlaces(AfkBatch afkBatch) {
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
        //
        if (argumentInfo.eInvasion || afkBatch.doInvasion)
            eventList.add(AttendablePlaces.invasion);
        if (argumentInfo.eExpedition || afkBatch.doExpedition)
            eventList.add(AttendablePlaces.expedition);
        if (argumentInfo.eGvg || afkBatch.doGvg)
            eventList.add(AttendablePlaces.gvg);
        if (argumentInfo.eTrials || afkBatch.doTrials)
            eventList.add(AttendablePlaces.trials);
        if (argumentInfo.eGauntlet || afkBatch.doGauntlet)
            eventList.add(AttendablePlaces.gauntlet);

        if (argumentInfo.ePvp || afkBatch.doPvp)
            eventList.add(AttendablePlaces.pvp);
        if (argumentInfo.eWorldBoss || afkBatch.doWorldBoss)
            eventList.add(AttendablePlaces.worldBoss);
        if (argumentInfo.eRaid || afkBatch.doRaid)
            eventList.add(AttendablePlaces.raid);
        //
        if (eventList.size() == 0) {
            final List<MenuItem> menuItems = Stream.of(
                    MenuItem.from(AttendablePlaces.pvp),
                    MenuItem.from(AttendablePlaces.worldBoss),
                    MenuItem.from(AttendablePlaces.raid),
                    MenuItem.from("GVG/Expedition/Invasion", AttendablePlaces.gvg, AttendablePlaces.expedition, AttendablePlaces.invasion),
                    MenuItem.from("Trials/Gauntlet", AttendablePlaces.trials, AttendablePlaces.gauntlet),
                    MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.raid),
                    MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.raid,
                            AttendablePlaces.invasion, AttendablePlaces.trials),
                    MenuItem.from("All", allAttendablePlaces.toArray(new AttendablePlace[0]))
            ).collect(Collectors.toList());

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
                Main.exit(0);
            }
        }

        eventList = eventList.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        info("Selected:");
        for (AttendablePlace event : eventList) {
            info("  <%3d> %s", event.id, event.name);
        }

        return eventList;
    }

    @Override
    protected String getUsage() {
        return "<(optional)tasks_combination>";
    }

    @Override
    protected String getDescription() {
        return "AFK automatically consume PVP/World Boss/GVG/Invasion/Trials/Gauntlet turns, thus you can away from keyboard. Argument is combination of " + shortDescArg;
    }

    @Override
    public String getArgHint() {
        return String.format("Argument is combination of the following values: %s. For example %s%s%s is PVP + World Boss + Raid", shortDescArg, codePvp, codeWorldBoss1, codeRaid);
    }

    @Override
    protected String getLimitationExplain() {
        return "This AFK function does not support select level/mode, how many badge/ticket/... to consumes and can only everything by default so please chose everything first manually then use this";
    }

    private static final char codePvp = 'P';
    private static final char codeWorldBoss1 = 'B';
    private static final char codeWorldBoss2 = 'W';
    private static final char codeRaid = 'R';
    private static final char codeInvasion = 'I';
    private static final char codeExpedition = 'E';
    private static final char codeGVG = 'V';
    private static final char codeTrials = 'T';
    private static final char codeGauntlet = 'G';
    private static final char codeComboPvpWorldBossRaid = '1';
    private static final char codeComboInvasionGvgExpedition = '2';
    private static final char codeComboTrialsGauntlet = '3';
    private static final char codeComboAll = 'A';

    private static final String shortDescArg = String.format("%s (PVP), %s (World Boss), %s (Raid), %s (Invasion), %s (Expedition), %s (GVG), %s (Gauntlet), %s (Trials), %s (PVP/World Boss/Raid), %s (Invasion/GVG/Expedition), %s (Trials/Gauntlet), %s (All)", codePvp, codeWorldBoss1, codeRaid, codeInvasion, codeExpedition, codeGVG, codeGauntlet, codeTrials, codeComboPvpWorldBossRaid, codeComboInvasionGvgExpedition, codeComboTrialsGauntlet, codeComboAll);

    private static class AfkBatch {
        public boolean doPvp;
        public boolean doWorldBoss;
        public boolean doRaid;
        public boolean doInvasion;
        public boolean doExpedition;
        public boolean doGvg;
        public boolean doTrials;
        public boolean doGauntlet;
    }

    private static AfkBatch getEventListFromArg(String[] args) {
        final AfkBatch result = new AfkBatch();
        String normalized = Arrays.stream(args).filter(StringUtil::isNotBlank).map(String::trim).map(String::toUpperCase).collect(Collectors.joining(","));
        if (StringUtil.isNotBlank(normalized)) {
            for (char c : normalized.toCharArray()) {
                if (c == codeComboAll) {
                    result.doPvp = true;
                    result.doWorldBoss = true;
                    result.doRaid = true;
                    result.doInvasion = true;
                    result.doExpedition = true;
                    result.doGvg = true;
                    result.doTrials = true;
                    result.doGauntlet = true;
                } else if (c == codeComboPvpWorldBossRaid) {
                    result.doPvp = true;
                    result.doWorldBoss = true;
                    result.doRaid = true;
                } else if (c == codeComboInvasionGvgExpedition) {
                    result.doInvasion = true;
                    result.doExpedition = true;
                    result.doGvg = true;
                } else if (c == codeComboTrialsGauntlet) {
                    result.doTrials = true;
                    result.doGauntlet = true;
                } else if (c == codePvp) {
                    result.doPvp = true;
                } else if (c == codeWorldBoss1 || c == codeWorldBoss2) {
                    result.doWorldBoss = true;
                } else if (c == codeRaid) {
                    result.doRaid = true;
                } else if (c == codeInvasion) {
                    result.doInvasion = true;
                } else if (c == codeGVG) {
                    result.doGvg = true;
                } else if (c == codeExpedition) {
                    result.doExpedition = true;
                } else if (c == codeTrials) {
                    result.doTrials = true;
                } else if (c == codeGauntlet) {
                    result.doGauntlet = true;
                } else if (c == ',' || c == ';' || c == '+') {
                    continue;
                } else {
                    throw new InvalidDataException("Unrecognized code value '%s'. Accepted values are combination of %s. For example combination '%s%s%s' stands for PVP + World Boss + Trials", c, shortDescArg, codePvp, codeWorldBoss1, codeTrials);
                }
            }
        }
        return result;
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 5_000;
    }
}