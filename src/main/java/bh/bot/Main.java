package bh.bot;

import bh.bot.app.*;
import bh.bot.app.dev.ExtractMatrixApp;
import bh.bot.app.dev.ImportTpImageApp;
import bh.bot.app.dev.ScreenCaptureApp;
import bh.bot.app.dev.TestApp;
import bh.bot.app.farming.*;
import bh.bot.common.Configuration;
import bh.bot.common.OS;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.flags.*;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.fusesource.jansi.AnsiConsole;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.Extensions.scriptFileName;
import static bh.bot.common.utils.StringUtil.isBlank;

@SuppressWarnings("deprecation")
public class Main {
	public static boolean forceDisableAnsi = false;
    public static void main(String[] args) {
    	try {
        	AnsiConsole.systemInstall();
    	} catch (Throwable t) {
    		System.err.println("Failure initialization ansi console! Error message: " + t.getMessage());
    		forceDisableAnsi = true;
    	}
        try {
            Configuration.registerApplicationClasses( //
                    SettingApp.class, //
//
                    ReRunApp.class, //
                    FishingApp.class, //
                    AfkApp.class, //
                    WorldBossApp.class, //
                    PvpApp.class, //
                    InvasionApp.class, //
                    ExpeditionApp.class, //
                    TrialsApp.class, //
                    GvgApp.class, //
                    GauntletApp.class, //
                    GenMiniClient.class,
//
                    ExtractMatrixApp.class, //
                    ImportTpImageApp.class, //
//
                    ScreenCaptureApp.class, //
                    TestApp.class//
            );

            if (args.length == 0 || Arrays.stream(args).allMatch(x -> x.trim().startsWith("--")))
                args = buildArgument(args);

            process(args);
        } catch (InvalidFlagException ex) {
            err(ex.getMessage());
            System.exit(EXIT_CODE_INVALID_FLAG);
        } catch (Exception ex) {
            ex.printStackTrace();
            err(ex.getMessage());
            System.exit(EXIT_CODE_UNHANDLED_EXCEPTION);
        }
    }

    private static String[] buildArgument(String[] args) {
        List<Tuple2<Class<? extends AbstractApplication>, AppMeta>> applicationClasses = Configuration.getApplicationClasses(Configuration.enableDevFeatures);
        StringBuilder sb = new StringBuilder("Available functions:\n");
        for (int i = 0; i < applicationClasses.size(); i++)
            sb.append(String.format("  %2d. %s\n", i + 1, applicationClasses.get(i)._2.name()));
        sb.append("Select a function you want to launch:");
        AppMeta meta = readInput(sb.toString(), null, s -> {
            try {
            	int num = Integer.parseInt(s);
            	return new Tuple3<>(true, null, applicationClasses.get(num - 1)._2);
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
				return new Tuple3<>(false, "Not a valid option, please try again", null);
            }
        });

        info(ColorizeUtil.formatInfo, "You selected function:");
        info(ColorizeUtil.formatAsk, "  %s", meta.name());

		ArrayList<String> lArgs = new ArrayList<>();
		lArgs.add(meta.code());
		lArgs.addAll(Arrays.stream(args).map(String::trim).filter(x -> x.startsWith("--")).distinct().collect(Collectors.toList()));

        FlagPrintHelpMessage flagHelp = new FlagPrintHelpMessage();
        if (lArgs.stream().anyMatch(x -> x.equalsIgnoreCase(flagHelp.getCode())))
            return lArgs.toArray(new String[0]);

        FlagSteamResolution800x480 flagSteam = new FlagSteamResolution800x480();
        if (flagSteam.isSupportedOnCurrentOsPlatform()) {
            String flagSteamCode = flagSteam.getCode();
            if (lArgs.stream().noneMatch(x -> x.equalsIgnoreCase(flagSteamCode))) {
                boolean enableSteam = readYesNoInput("Steam client?", "Press 'Y' to launch this app on Steam mode (800x480) or press 'N' to launch this app on Mini-client mode (800x520)");
                if (enableSteam)
                    lArgs.add(flagSteamCode);
            }
        }

        boolean addMoreFlags = readYesNoInput("Do you want to add some add some flags? (Yes/No)", String.format("You can pass flags like '--exit=3600'/'--steam'/'--all'/'--help'... here. For list of supported flags available for each function, please run file '%s'", scriptFileName("help")));
        if (addMoreFlags) {
            final Supplier<List<String>> selectedFlagsInfoProvider = () -> lArgs.stream().filter(x -> x.startsWith("--")).collect(Collectors.toList());
            while (true) {
                String newFlag = readInput("Type flag here, you can just leave this empty and type Enter to finish", "there is no validation here so you have to type the flag correctly, otherwise error will be raised upon starting app", selectedFlagsInfoProvider, s -> {
                    String flag = s.trim().toLowerCase();
                    if (!flag.startsWith("--") || flag.length() < 3 || flag.startsWith("---"))
                        return new Tuple3<>(false, "Wrong format, a flag must starts with '--', for example: '--help' (without quotes)", null);
                    if (flag.contains(" "))
                        return new Tuple3<>(false, "Can not contains space", null);
                    if (flag.endsWith("="))
                        return new Tuple3<>(false, "Can not ends with '='", null);
                    return new Tuple3<>(true, null, flag);
                }, true);
                if (isBlank(newFlag))
                    break;
                lArgs.add(newFlag);
            }
        }

		return lArgs.toArray(new String[0]);
	}

    private static void process(String[] args) throws Exception {
        ParseArgumentsResult parseArgumentsResult = parseArguments(args);

        if (parseArgumentsResult.enableDebugMessages)
            enableDebug();
        if (parseArgumentsResult.disableTelegramNoti)
            Telegram.disable();

        Configuration.loadSystemConfig(parseArgumentsResult);
        InteractionUtil.init();

        Constructor<?> cons = parseArgumentsResult.applicationClass.getConstructors()[0];
        AbstractApplication instance = (AbstractApplication) cons.newInstance();

        if (parseArgumentsResult.displayHelp) {
            System.out.println(instance.getHelp());
            info("With flag '--help' provided, application will exit immediately");
            return;
        }

        //noinspection rawtypes
        for (FlagPattern flagPattern : parseArgumentsResult.usingFlags)
            if (!flagPattern.isSupportedByApp(instance)) {
                System.out.println(instance.getHelp());
                throw new InvalidFlagException(String.format("Flag '--%s' does not supported by '%s'",
                        flagPattern.getName(), instance.getAppCode()));
            }

        instance.run(parseArgumentsResult);
    }

    @SuppressWarnings("rawtypes")
    private static ParseArgumentsResult parseArguments(String[] args)
            throws InvalidFlagException {
        String appCode = args[0];

        FlagPattern[] flagPatterns = Flags.allFlags;

        List<String> rawFlags = Arrays.stream(args).map(String::trim).filter(x -> x.startsWith("--"))
                .collect(Collectors.toList());

        ArrayList<FlagPattern> usingFlagPatterns = new ArrayList<>();

        // Check flags
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

        // Parse param
        int exitAfter = 0;
        int profileNumber = -1;
        for (FlagPattern flagPattern : usingFlagPatterns) {
            if (!flagPattern.isAllowParam())
                continue;

            if (flagPattern instanceof FlagExitAfterAmountOfSeconds) {
                exitAfter = ((FlagExitAfterAmountOfSeconds) flagPattern).parseParams().get(0);
                continue;
            }

            if (flagPattern instanceof FlagProfileNo) {
                profileNumber = ((FlagProfileNo) flagPattern).parseParams().get(0);
                continue;
            }

            throw new NotImplementedException(
                    String.format("Not implemented extracting param for flag '--%s' (Class: %s)", flagPattern.getName(),
                            flagPattern.getClass().getSimpleName()));
        }

        if (exitAfter >= 3600) {
            int h = exitAfter / 3600;
            int m = (exitAfter - h * 3600) / 60;
            info("Application will exit after %d hours and %d minutes", h, m);
        }

        // Validate flags
        for (FlagPattern flagPattern : usingFlagPatterns)
            if (!flagPattern.isSupportedOnCurrentOsPlatform())
                throw new InvalidFlagException(String.format("Flag '--%s' is not supported on %s",
                        flagPattern.getName(), OS.name));

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
            screenResolutionProfile = is800x480Resolution ? new ScreenResolutionProfile.SteamProfile()
                    : new ScreenResolutionProfile.WebProfile();
        }

        args = Arrays.stream(args).skip(1).filter(x -> !x.startsWith("--")).toArray(String[]::new);

        Class<? extends AbstractApplication> applicationClassFromAppCode = Configuration
                .getApplicationClassFromAppCode(appCode);

        if (applicationClassFromAppCode == null)
            throw new IllegalArgumentException("First argument must be a valid app code");

        ParseArgumentsResult li = new ParseArgumentsResult(applicationClassFromAppCode, args, usingFlagPatterns);
        li.exitAfterXSecs = exitAfter;
        li.displayHelp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagPrintHelpMessage);
        li.enableSavingDebugImages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagSaveDebugImages);
        li.enableDebugMessages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagShowDebugMessages);
        li.disableTelegramNoti = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagMuteNoti);
        li.screenResolutionProfile = screenResolutionProfile;
        li.profileNumber = profileNumber;
        li.hasFlagAll = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagAll);
        // events
        li.eWorldBoss = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoWorldBoss);
        li.ePvp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoPvp);
        li.eInvasion = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoInvasion);
        li.eExpedition = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoExpedition);
        li.eGvg = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoGvG);
        li.eTrials = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoTrials);
        li.eGauntlet = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoGauntlet);
        li.eRaid = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoRaid);
        // end events
        return li;
    }

    private static BufferedReader bufferedReader = null;

    public static synchronized BufferedReader getBufferedReader() {
        if (bufferedReader == null)
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        return bufferedReader;
    }

    public static <T> T readInput(String ask, String desc,
                                  Function<String, Tuple3<Boolean, String, T>> transform) {
        return readInput(ask, desc, transform, false);
    }

    public static <T> T readInput(String ask, String desc,
                                  Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
        return readInput(ask, desc, null, transform, allowBlankAndIfBlankThenReturnNull);
    }

    public static boolean readYesNoInput(String ask, String desc) {
    	return readInput(ask, desc == null ? "Answer by typing Y/N" : desc, s -> {
			String answer = s.trim().toLowerCase();
			if ("yes".equals(answer) || "y".equals(answer))
				return new Tuple3<>(true, null, true);
			if ("no".equals(answer) || "n".equals(answer))
				return new Tuple3<>(true, null, false);
			return new Tuple3<>(false, "Not a valid answer", null);
		});
	}

    public static <T> T readInput(String ask, String desc,
                                  Supplier<List<String>> selectedOptionsInfoProvider, Function<String, Tuple3<Boolean, String, T>> transform,
                                  boolean allowBlankAndIfBlankThenReturnNull) {
        try {
            BufferedReader br = Main.getBufferedReader();
            String input;
            String errMessage = null;
            while (true) {
                info("\n\n==================================");
                info(ColorizeUtil.formatAsk, ask);
                if (selectedOptionsInfoProvider != null) {
                    List<String> selectedOptions = selectedOptionsInfoProvider.get();
                    if (selectedOptions.size() > 0)
                        info("Selected: %s", String.join(", ", selectedOptions));
                }
                if (desc != null)
                    info("(%s)", desc);
                if (errMessage != null)
                    err(errMessage);
                warn("Please complete the above question first, otherwise bot will be hanged here!!!");
                input = br.readLine();

                if (isBlank(input)) {
                    if (allowBlankAndIfBlankThenReturnNull)
                        return null;
                    errMessage = "You inputted nothing, please try again!";
                    continue;
                }

                Tuple3<Boolean, String, T> tuple = transform.apply(input);
                if (!tuple._1) {
                    errMessage = tuple._2;
                    continue;
                }

                return tuple._3;
            }
        } catch (IOException e) {
            e.printStackTrace();
            err("Error while reading input, application is going to exit now, please try again later");
            System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            return null;
        }
    }

    public static final int EXIT_CODE_SCREEN_RESOLUTION_ISSUE = 3;
    public static final int EXIT_CODE_FAILURE_READING_INPUT = 5;
    public static final int EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS = 6;
    public static final int EXIT_CODE_EXTERNAL_REASON = 7;
    public static final int EXIT_CODE_INVALID_FLAG = 8;
    public static final int EXIT_CODE_UNABLE_DETECTING_FISHING_ANCHOR = 9;
    public static final int EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION = 11;
    public static final int EXIT_CODE_UNHANDLED_EXCEPTION = -1;
}
