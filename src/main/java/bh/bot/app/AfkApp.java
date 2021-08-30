package bh.bot.app;

import bh.bot.Main;
import bh.bot.app.farming.*;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.types.AttendablePlace.MenuItem;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppCode(code = "afk")
public class AfkApp extends AbstractApplication {
    private InteractionUtil.Screen.Game gameScreenInteractor;
    private final AtomicLong blockPvpUntil = new AtomicLong(0);
    private final AtomicLong blockWorldBossUntil = new AtomicLong(0);
    private final AtomicLong blockRaidUntil = new AtomicLong(0);
    private final AtomicLong blockGvgAndInvasionUntil = new AtomicLong(0);
    private final AtomicLong blockTrialsAndGauntletUntil = new AtomicLong(0);

    @Override
    protected void internalRun(String[] args) {
        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        ArrayList<AttendablePlace> eventList = getAttendablePlaces();
        if (eventList.contains(AttendablePlaces.raid))
            throw new NotSupportedException("Raid has not been supported");
        //
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoop(
                        masterSwitch,
                        eventList.contains(AttendablePlaces.pvp),
                        eventList.contains(AttendablePlaces.worldBoss),
                        eventList.contains(AttendablePlaces.raid),
                        eventList.contains(AttendablePlaces.gvg),
                        eventList.contains(AttendablePlaces.invasion),
                        eventList.contains(AttendablePlaces.trials),
                        eventList.contains(AttendablePlaces.gauntlet)
                ),
                () -> doClickTalk(masterSwitch::get),
                () -> detectDisconnected(masterSwitch),
                () -> autoReactiveAuto(masterSwitch),
                () -> autoExit(argumentInfo.exitAfterXSecs, masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoop(
            AtomicBoolean masterSwitch,
            boolean doPvp,
            boolean doWorldBoss,
            boolean doRaid,
            boolean doGvg,
            boolean doInvasion,
            boolean doTrials,
            boolean doGauntlet
    ) {
        info("Begin AFK");
        boolean isUnknownGvgOrInvasion = doGvg && doInvasion;
        boolean isUnknownTrialsOrGauntlet = doTrials && doGauntlet;
        int continuousNotFound = 0;
        final Point coordinateHideMouse = new Point(0, 0);
        final ArrayList<Tuple3<AttendablePlace, AtomicLong, List<AbstractDoFarmingApp.NextAction>>> taskList = new ArrayList<>();
        if (doPvp)
            taskList.add(new Tuple3<>(AttendablePlaces.pvp, blockPvpUntil, PvpApp.getPredefinedImageActions()));
        if (doWorldBoss)
            taskList.add(new Tuple3<>(AttendablePlaces.worldBoss, blockWorldBossUntil, WorldBossApp.getPredefinedImageActions()));
        if (doRaid)
            taskList.add(new Tuple3<>(AttendablePlaces.raid, blockRaidUntil, getPredefinedImageActionsOfRaid()));
        if (doGvg)
            taskList.add(new Tuple3<>(AttendablePlaces.gvg, blockGvgAndInvasionUntil, GvgApp.getPredefinedImageActions()));
        if (doInvasion)
            taskList.add(new Tuple3<>(AttendablePlaces.invasion, blockGvgAndInvasionUntil, InvasionApp.getPredefinedImageActions()));
        if (doTrials)
            taskList.add(new Tuple3<>(AttendablePlaces.trials, blockTrialsAndGauntletUntil, TrialsApp.getPredefinedImageActions()));
        if (doGauntlet)
            taskList.add(new Tuple3<>(AttendablePlaces.gauntlet, blockTrialsAndGauntletUntil, GauntletApp.getPredefinedImageActions()));

        ArrayList<AbstractDoFarmingApp.NextAction> outOfTurnNextActionList = new ArrayList<>();
        addOutOfTurnActionsToList(outOfTurnNextActionList, PvpApp.getPredefinedImageActions());
        addOutOfTurnActionsToList(outOfTurnNextActionList, WorldBossApp.getPredefinedImageActions());
        addOutOfTurnActionsToList(outOfTurnNextActionList, getPredefinedImageActionsOfRaid());
        addOutOfTurnActionsToList(outOfTurnNextActionList, GvgApp.getPredefinedImageActions());
        addOutOfTurnActionsToList(outOfTurnNextActionList, InvasionApp.getPredefinedImageActions());
        addOutOfTurnActionsToList(outOfTurnNextActionList, TrialsApp.getPredefinedImageActions());
        addOutOfTurnActionsToList(outOfTurnNextActionList, GauntletApp.getPredefinedImageActions());

        AttendablePlace toBeRemoved = null;

        final byte minutesSleepWaitingResourceGeneration = 5;
        final short loopSleep = 5_000;
        final short originalCheckAreYouStillThereAfter = 20_000 / loopSleep;
        short checkAreYouStillThereAfter = originalCheckAreYouStillThereAfter;
        final short originalSleepWhileWaitingResourceRegen = 5 * 60_000 / loopSleep;
        short sleepWhileWaitingResourceRegen = 0;

        ML:
        while (!masterSwitch.get()) {
            sleep(loopSleep);

            if (toBeRemoved != null) {
                final AttendablePlace target = toBeRemoved;
                if (taskList.removeIf(x -> x._1 == target)) {
                    toBeRemoved = null;
                    debug("Removed %s from taskList", target.name);
                    continue ML;
                }
            }

            if (--checkAreYouStillThereAfter <= 0) {

                if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.areYouStillThere)) {
                    info("Knock knock, are you still there?");
                    InteractionUtil.Keyboard.sendEnter();
                    sleep(1_000);
                    InteractionUtil.Keyboard.sendEscape();
                    checkAreYouStillThereAfter = 2;
                } else {
                    checkAreYouStillThereAfter = originalCheckAreYouStillThereAfter;
                }
                continue ML;
            }

            if (sleepWhileWaitingResourceRegen > 0) {
                sleepWhileWaitingResourceRegen--;
                continue ML;
            }

            if (taskList.stream().allMatch(x -> !isNotBlocked(x._2))) {
                info("Waiting for resource generation, sleeping %d minutes", minutesSleepWaitingResourceGeneration);
                sleepWhileWaitingResourceRegen = originalSleepWhileWaitingResourceRegen;
                continue ML;
            }

            if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                debug("confirmStartNotFullTeam");
                InteractionUtil.Keyboard.sendSpaceKey();
                continuousNotFound = 0;
                moveCursor(coordinateHideMouse);
                continue ML;
            }

            if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmQuitBattle)) {
                debug("confirmQuitBattle");
                InteractionUtil.Keyboard.sendEnter();
                sleep(1_000);
                spamEscape(1);
                continuousNotFound = 0;
                moveCursor(coordinateHideMouse);
                continue ML;
            } else {
                debug("confirmQuitBattle not found");
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
                        if (isUnknownGvgOrInvasion) {
                            if (tuple._1 == AttendablePlaces.gvg) {
                                isUnknownGvgOrInvasion = false;
                                toBeRemoved = AttendablePlaces.invasion;
                            }
                            if (tuple._1 == AttendablePlaces.invasion) {
                                isUnknownGvgOrInvasion = false;
                                toBeRemoved = AttendablePlaces.gvg;
                            }
                        }
                        if (isUnknownTrialsOrGauntlet) {
                            if (tuple._1 == AttendablePlaces.trials) {
                                isUnknownTrialsOrGauntlet = false;
                                toBeRemoved = AttendablePlaces.gauntlet;
                            }
                            if (tuple._1 == AttendablePlaces.gauntlet) {
                                isUnknownTrialsOrGauntlet = false;
                                toBeRemoved = AttendablePlaces.trials;
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
    }

    private void addOutOfTurnActionsToList(ArrayList<AbstractDoFarmingApp.NextAction> list, List<AbstractDoFarmingApp.NextAction> predefinedNextActions) {
        list.addAll(predefinedNextActions.stream().filter(x -> x.isOutOfTurns).collect(Collectors.toList()));
    }

    private AbstractDoFarmingApp.NextAction tryToClickOnBatch(List<AbstractDoFarmingApp.NextAction> predefinedImageActions) {
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
        else if (attendablePlace == AttendablePlaces.gvg || attendablePlace == AttendablePlaces.invasion)
            x = blockGvgAndInvasionUntil;
        else if (attendablePlace == AttendablePlaces.trials || attendablePlace == AttendablePlaces.gauntlet)
            x = blockTrialsAndGauntletUntil;
        else
            throw new NotSupportedException(String.format("Not supported AttendablePlace.%s", attendablePlace.name));
        x.set(System.currentTimeMillis() + attendablePlace.procedureTicketMinutes * 60_000);
    }

    private ArrayList<AttendablePlace> getAttendablePlaces() {
        ArrayList<AttendablePlace> eventList = new ArrayList<>();
        final List<AttendablePlace> allAttendablePlaces = Arrays.asList(
                AttendablePlaces.invasion,
                AttendablePlaces.trials,
                AttendablePlaces.gvg,
                AttendablePlaces.gauntlet,

                AttendablePlaces.pvp,
                AttendablePlaces.worldBoss
                // TODO RAID AttendablePlaces.raid
        );
        if (argumentInfo.hasFlagAll)
            eventList.addAll(allAttendablePlaces);
        //
        if (argumentInfo.eInvasion)
            eventList.add(AttendablePlaces.invasion);
        if (argumentInfo.eTrials)
            eventList.add(AttendablePlaces.trials);

        if (argumentInfo.ePvp)
            eventList.add(AttendablePlaces.pvp);
        if (argumentInfo.eWorldBoss)
            eventList.add(AttendablePlaces.worldBoss);
        if (argumentInfo.eRaid)
            eventList.add(AttendablePlaces.raid);
        //
        if (eventList.size() == 0) {

            final List<MenuItem> menuItems = Stream.concat(
                    allAttendablePlaces.stream().map(x -> MenuItem.from(x)),
                    Arrays.asList(
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss),
                            MenuItem.from(AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.gvg, AttendablePlaces.gauntlet),
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.gvg, AttendablePlaces.gauntlet, AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.gvg, AttendablePlaces.gauntlet)
                    ).stream()
            ).collect(Collectors.toList());

            String menuItem = String
                    .join("\n", menuItems.stream().map(x -> String.format("  %3d. %s", x.num, x.name))
                            .collect(Collectors.toList()));

            final ArrayList<AttendablePlace> selectedOptions = new ArrayList<>();
            final Supplier<List<String>> selectedOptionsInfoProvider = () -> selectedOptions.stream().map(x -> x.name).collect(Collectors.toList());

            String ask = "Select events you want to do:\n" + menuItem;
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                while (true) {
                    List<AttendablePlace> events = readInput(br, ask, "To select an event, press the number then press Enter. To finish input, just enter without supply a number", selectedOptionsInfoProvider, new Function<String, Tuple3<Boolean, String, List<AttendablePlace>>>() {
                        @Override
                        public Tuple3<Boolean, String, List<AttendablePlace>> apply(String s) {
                            try {
                                int result = Integer.parseInt(s);
                                List<AttendablePlace> events = allAttendablePlaces.stream().filter(x -> (result & x.id) == x.id).collect(Collectors.toList());
                                if (events.size() == 0)
                                    return new Tuple3<>(false, "Incorrect value", null);
                                return new Tuple3<>(true, null, events);
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse your input, error: " + ex2.getMessage(), null);
                            }
                        }
                    }, true);

                    if (events == null)
                        break;
                    eventList.addAll(events);
                    eventList = new ArrayList<>(eventList.stream().distinct().collect(Collectors.toList()));
                    selectedOptions.clear();
                    selectedOptions.addAll(eventList);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            }

            if (eventList.size() == 0) {
                info("None option was selected, exit now");
                System.exit(0);
            }
        }

        eventList = new ArrayList<>(eventList.stream().distinct().collect(Collectors.toList()));

        info("Selected:");
        for (AttendablePlace event : eventList) {
            info("  <%2d> %s", event.id, event.name);
        }

        return eventList;
    }

    private void spamEscape(int expectedCount) {
        int cnt = expectedCount + 4;
        while (cnt-- > 0) {
            sleep(1_000);
            InteractionUtil.Keyboard.sendEscape();
        }
    }

    @Override
    protected String getAppName() {
        return "BH-AFK";
    }

    @Override
    protected String getScriptFileName() {
        return "afk";
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
        // TODO Afk Raid
        return new ArrayList<>();
    }
}
