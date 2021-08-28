package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

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

import static bh.bot.common.Log.info;
import static bh.bot.common.types.AttendablePlace.MenuItem;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppCode(code = "afk")
public class AfkApp extends AbstractApplication {
    private InteractionUtil.Screen.Game gameScreenInteractor;
    private final AtomicBoolean isUnknownGvgOrInvasion = new AtomicBoolean();
    private final AtomicBoolean isUnknownTrialsOrGauntlet = new AtomicBoolean();
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
        isUnknownGvgOrInvasion.set(eventList.contains(AttendablePlaces.gvg) && eventList.contains(AttendablePlaces.invasion));
        isUnknownTrialsOrGauntlet.set(eventList.contains(AttendablePlaces.trials) && eventList.contains(AttendablePlaces.gauntlet));
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
        while (!masterSwitch.get()) {
            sleep(5_000);
            long epoch = System.currentTimeMillis();

        }
    }

    private void blockMinutes(AttendablePlace attendablePlace) {
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
                AttendablePlaces.worldBoss,
                AttendablePlaces.raid
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
                            MenuItem.from(AttendablePlaces.raid, AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.raid, AttendablePlaces.pvp, AttendablePlaces.worldBoss, AttendablePlaces.gvg, AttendablePlaces.gauntlet),
                            MenuItem.from(AttendablePlaces.raid, AttendablePlaces.pvp, AttendablePlaces.worldBoss),
                            MenuItem.from(AttendablePlaces.invasion, AttendablePlaces.trials),
                            MenuItem.from(AttendablePlaces.gvg, AttendablePlaces.gauntlet)
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
        return "Burns your turns while you are AFK";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
