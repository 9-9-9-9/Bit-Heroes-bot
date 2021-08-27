package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.InteractionUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.types.AttendablePlace.MenuItem;

@AppCode(code = "afk")
public class AfkApp extends AbstractApplication {
    private InteractionUtil.Screen.Game gameScreenInteractor;

    @Override
    protected void internalRun(String[] args) {
        this.gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
        ArrayList<AttendablePlace> eventList = getAttendablePlaces();
        //
        debug("Scanning screen");
        for (AttendablePlace event : eventList) {
            Point point = this.gameScreenInteractor.findAttendablePlace(event);
            if (point == null)
                continue;
            debug("Found event id %2d at %3d,%3d", event.id, point.x, point.y);
        }
        debug("End");
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
