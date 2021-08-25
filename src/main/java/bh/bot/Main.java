package bh.bot;

import bh.bot.app.*;
import bh.bot.common.Configuration;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.utils.InteractionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ScreenResolutionProfile screenResolutionProfile = getScreenResolutionProfile(args);
        if (screenResolutionProfile == null) {
            System.exit(EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
        }

        Configuration.load(screenResolutionProfile);
        Configuration.registerApplicationInstances(
                new ReRunApp(),
                new FishingApp(),
                new AfkApp(),
                new GenMiniClient(),
                //
                new KeepPixApp(),
                new ExtractMatrixApp(),
                new SamePixApp(),
                //
                new TestApp()
        );
        InteractionUtil.init();
        AbstractApplication.LaunchInfo launchInfo = AbstractApplication.parse(args);
        if (launchInfo.displayHelp) {
            System.out.println(launchInfo.instance.getHelp());
            return;
        }
        launchInfo.instance.run(launchInfo);
    }

    private static ScreenResolutionProfile getScreenResolutionProfile(String[] args) {
        List<String> l = Arrays.asList(args).stream().map(String::toLowerCase).collect(Collectors.toList());
        boolean isWeb = l.contains("--web");
        boolean isSteam = l.contains("--steam");
        ScreenResolutionProfile.WebProfile webProfile = new ScreenResolutionProfile.WebProfile();
        ScreenResolutionProfile.SteamProfile steamProfile = new ScreenResolutionProfile.SteamProfile();
        if (isWeb && isSteam) {
            err("Ambiguous profile, must specify only one of 2 profiles:");
            err("  '--web' which supports game resolution %dx%d", webProfile.getSupportedGameResolutionWidth(), webProfile.getSupportedGameResolutionHeight());
            err("  '--steam' which supports game resolution %dx%d", steamProfile.getSupportedGameResolutionWidth(), steamProfile.getSupportedGameResolutionHeight());
            return null;
        }

        if (!isWeb && !isSteam) {
            info("No screen profile specified, `--web` profile has been chosen by default");
            return webProfile;
        }

        return isSteam
                ? steamProfile
                : webProfile;
    }

    public static final int EXIT_CODE_SCREEN_RESOLUTION_ISSUE = 3;
    public static final int EXIT_CODE_FAILURE_READING_INPUT = 5;
    public static final int EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS = 6;
    public static final int EXIT_CODE_EXTERNAL_REASON = 7;
    // 20 to 40 for fishing
    public static final int EXIT_CODE_UNABLE_DETECTING_ANCHOR = 20;
    // end fishing
    public static final int EXIT_CODE_UNHANDLED_EXCEPTION = -1;
}
