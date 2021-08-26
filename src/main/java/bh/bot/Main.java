package bh.bot;

import bh.bot.app.*;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.LaunchInfo;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.flags.*;
import bh.bot.common.utils.InteractionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

public class Main {
    public static void main(String[] args) {
        try {

            Configuration.registerApplicationInstances(
                    ReRunApp.class,
                    FishingApp.class,
                    AfkApp.class,
                    WorldBoss.class,
                    PvpApp.class,
                    InvasionApp.class,
                    TrialsApp.class,
                    GenMiniClient.class,
//
                    KeepPixApp.class,
                    ExtractMatrixApp.class,
                    SamePixApp.class,
//
                    ScreenCaptureApp.class,
                    TestApp.class
            );
            LaunchInfo launchInfo = parse(args);
            if (launchInfo.displayHelp) {
                System.out.println(launchInfo.instance.getHelp());
                return;
            }
            if (launchInfo.enableDebugMessages)
                Log.enableDebug();
            if (launchInfo.disableTelegramNoti)
                Telegram.disable();

            Configuration.load(launchInfo.screenResolutionProfile);
            InteractionUtil.init();

            launchInfo.instance.run(launchInfo);
        } catch (InvalidFlagException ex) {
            err(ex.getMessage());
            System.exit(EXIT_CODE_INVALID_FLAG);
        } catch (Exception ex) {
            ex.printStackTrace();
            err(ex.getMessage());
            System.exit(EXIT_CODE_UNHANDLED_EXCEPTION);
        }
    }

    private static LaunchInfo parse(String[] args) throws InvalidFlagException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String appCode = args[0];

        List<FlagPattern> flagPatterns = Arrays.asList(Flags.allFlags);

        List<String> rawFlags = Arrays
                .stream(args)
                .map(x -> x.trim())
                .filter(x -> x.startsWith("--"))
                .collect(Collectors.toList());

        ArrayList<FlagPattern> usingFlagPatterns = new ArrayList<>();

        for (String rawFlag : rawFlags) {
            boolean isAFlagPattern = false;
            for (FlagPattern flagPattern : flagPatterns) {
                if (flagPattern.isThisFlag(rawFlag)) {
                    flagPattern.pushRaw(rawFlag);
                    usingFlagPatterns.add(flagPattern);
                    isAFlagPattern = true;
                }
            }
            if (!isAFlagPattern)
                throw new InvalidFlagException(String.format("Flag '%s' can not be recognized", rawFlag));
        }

        int exitAfter = 0;
        for (FlagPattern flagPattern : usingFlagPatterns) {
            if (!flagPattern.isAllowParam())
                continue;

            if (flagPattern instanceof FlagExitAfterAmountOfSeconds) {
                exitAfter = ((FlagExitAfterAmountOfSeconds) flagPattern).parseParams().get(0);
                continue;
            }

            throw new NotImplementedException(String.format("Not implemented extracting param for flag '--%s' (Class: %s)", flagPattern.getName(), flagPattern.getClass().getSimpleName()));
        }

        if (exitAfter >= 3600) {
            int h = exitAfter / 3600;
            int m = (exitAfter - h * 3600) / 60;
            info("Application will exit after %d hours and %d minutes", h, m);
        }

        ScreenResolutionProfile screenResolutionProfile;
        boolean is800x480Resolution = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagSteamResolution800x480);
        boolean is800x520Resolution = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagWebResolution800x520);
        if (is800x480Resolution && is800x520Resolution) {
            err("Ambiguous profile, must specify only one of 2 profiles:");
            err("  '--web' which supports game resolution 800x520");
            err("  '--steam' which supports game resolution 800x480");
            System.exit(EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
        }

        if (!is800x480Resolution && !is800x520Resolution) {
            info("No screen profile specified, `--web` profile has been chosen by default");
            screenResolutionProfile = new ScreenResolutionProfile.WebProfile();
        } else {
            screenResolutionProfile = is800x480Resolution
                    ? new ScreenResolutionProfile.SteamProfile()
                    : new ScreenResolutionProfile.WebProfile();
        }

        args = Arrays
                .stream(args)
                .skip(1)
                .filter(x -> !x.startsWith("--"))
                .toArray(String[]::new);

        AbstractApplication instance = Configuration.getInstanceFromAppCode(appCode);

        if (instance == null)
            throw new IllegalArgumentException("First argument must be a valid app name");

        for (FlagPattern flagPattern : usingFlagPatterns)
            if (!flagPattern.isSupportedByApp(instance))
                throw new InvalidFlagException(String.format("Flag '--%s' does not supported by '%s'", flagPattern.getName(), instance.getAppCode()));

        LaunchInfo li = new LaunchInfo(instance, args);
        li.exitAfterXSecs = exitAfter;
        li.displayHelp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagPrintHelpMessage);
        li.enableSavingDebugImages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagSaveDebugImages);
        li.enableDebugMessages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagShowDebugMessages);
        li.disableTelegramNoti = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagMuteNoti);
        li.screenResolutionProfile = screenResolutionProfile;
        // events
        li.eWorldBoss = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoWorldBoss);
        li.ePvp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoPvp);
        li.eInvasion = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoInvasion);
        li.eTrials = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoTrials);
        li.eRaid = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoRaid);
        // end events
        return li;
    }

    public static final int EXIT_CODE_SCREEN_RESOLUTION_ISSUE = 3;
    public static final int EXIT_CODE_FAILURE_READING_INPUT = 5;
    public static final int EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS = 6;
    public static final int EXIT_CODE_EXTERNAL_REASON = 7;
    public static final int EXIT_CODE_INVALID_FLAG = 8;
    // 20 to 40 for fishing
    public static final int EXIT_CODE_UNABLE_DETECTING_ANCHOR = 20;
    // end fishing
    public static final int EXIT_CODE_UNHANDLED_EXCEPTION = -1;
}
