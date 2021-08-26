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
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;

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
        //
        if (launchInfo.eInvasion)
            eventList.add(AttendablePlaces.invasion);
        if (launchInfo.eTrials)
            eventList.add(AttendablePlaces.trials);

        if (launchInfo.ePvp)
            eventList.add(AttendablePlaces.pvp);
        if (launchInfo.eWorldBoss)
            eventList.add(AttendablePlaces.worldBoss);
        if (launchInfo.eRaid)
            eventList.add(AttendablePlaces.raid);
        //
        if (eventList.size() == 0) {
            final ArrayList<AttendablePlace> tmpAttendablePlaceList = new ArrayList<>();
            final List<AttendablePlace> allAttendablePlaces = Arrays.asList(
                    AttendablePlaces.invasion,
                    AttendablePlaces.trials,

                    AttendablePlaces.pvp,
                    AttendablePlaces.worldBoss,
                    AttendablePlaces.raid
            );
            info("Select events you want to do:");
            for (AttendablePlace event : allAttendablePlaces.stream().sorted(Comparator.comparingInt(AttendablePlace::getId)).collect(Collectors.toList()))
                info("  %2d. %s", event.id, event.name);
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                while (true) {
                    AttendablePlace event = readInput(br, "Input event code", "To select an event, press the number then press Enter. To finish input, just enter without supply a number", new Function<String, Tuple3<Boolean, String, AttendablePlace>>() {
                        @Override
                        public Tuple3<Boolean, String, AttendablePlace> apply(String s) {
                            try {
                                int result = Integer.parseInt(s);
                                Optional<AttendablePlace> first = allAttendablePlaces.stream().filter(x -> x.id == result).findFirst();
                                if (!first.isPresent())
                                    return new Tuple3<>(false, "ID does not exists", null);
                                AttendablePlace ev = first.get();
                                if (tmpAttendablePlaceList.stream().anyMatch(x -> x.id == ev.id))
                                    return new Tuple3<>(false, String.format("%s had been chosen before", ev.name), null);
                                return new Tuple3<>(true, null, ev);
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse your input, error: " + ex2.getMessage(), null);
                            }
                        }
                    }, true);

                    if (event == null)
                        break;

                    tmpAttendablePlaceList.add(event);
                    info("Selected event %s", event.name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            }

            eventList = new ArrayList<>(tmpAttendablePlaceList.stream().distinct().collect(Collectors.toList()));

            if (eventList.size() == 0) {
                info("No events supplied");
                System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            }
        }

        info("Selected events:");
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
    protected String getFlags() {
        return buildFlags(
                "--invasion : do Invasion",
                "--trials : do Trials",
                "--pvp : do PVP",
                "--boss : do World Boss",
                "--raid : do Raid",
                "--exit=X : stop after X seconds"
        );
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
