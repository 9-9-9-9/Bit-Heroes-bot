package bh.bot;

import static bh.bot.common.Log.enableDebug;
import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;
import static bh.bot.common.utils.Extensions.scriptFileName;
import static bh.bot.common.utils.StringUtil.isBlank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import bh.bot.app.dev.*;
import bh.bot.common.types.flags.*;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.fusesource.jansi.AnsiConsole;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.FishingApp;
import bh.bot.app.GenMiniClient;
import bh.bot.app.ReRunApp;
import bh.bot.app.SettingApp;
import bh.bot.app.farming.ExpeditionApp;
import bh.bot.app.farming.GauntletApp;
import bh.bot.app.farming.GvgApp;
import bh.bot.app.farming.InvasionApp;
import bh.bot.app.farming.PvpApp;
import bh.bot.app.farming.RaidApp;
import bh.bot.app.farming.TrialsApp;
import bh.bot.app.farming.WorldBossApp;
import bh.bot.common.Configuration;
import bh.bot.common.OS;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.Familiar;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.TimeUtil;
import bh.bot.common.utils.VersionUtil;
import bh.bot.common.utils.ColorizeUtil.Cu;

@SuppressWarnings("deprecation")
public class Main {
	public static final String botName = "99bot";
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
					RaidApp.class, //
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
					TestApp.class, //
					GenerateMetaApp.class
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
		List<Tuple2<Class<? extends AbstractApplication>, AppMeta>> applicationClasses = Configuration
				.getApplicationClasses(Configuration.enableDevFeatures);
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
		lArgs.addAll(Arrays.stream(args).map(String::trim).filter(x -> x.startsWith("--")).distinct()
				.collect(Collectors.toList()));

		FlagPrintHelpMessage flagHelp = new FlagPrintHelpMessage();
		if (lArgs.stream().anyMatch(x -> x.equalsIgnoreCase(flagHelp.getCode())))
			return lArgs.toArray(new String[0]);

		FlagPlayOnSteam flagPlayOnSteam = new FlagPlayOnSteam();
		FlagPlayOnWeb flagPlayOnWeb = new FlagPlayOnWeb();
		if (flagPlayOnSteam.isSupportedOnCurrentOsPlatform()) {
			lArgs.add(readInput("Steam or Web?\n\t1. Steam\n\t2.Web", null, s -> {
				try {
					int opt = Integer.parseInt(s.trim());
					if (opt == 1)
						return new Tuple3<>(true, null, flagPlayOnSteam.getCode());
					if (opt == 2)
						return new Tuple3<>(true, null, flagPlayOnWeb.getCode());
				} catch (NumberFormatException ex) {
					// ignored
				}
				return new Tuple3<>(false, "Wrong answer, must be <1> for Steam or <2> or Web", null);
			}));
		} else {
			lArgs.add(flagPlayOnWeb.getCode());
		}

		boolean addMoreFlags = readYesNoInput("Do you want to add some add some flags? (Y/N, empty is No)",
				String.format(
						"You can pass flags like '--exit=3600'/'--ear'/'--help'... here. For list of supported flags available for each function, please run file '%s'",
						scriptFileName("help")),
				true);
		if (addMoreFlags) {
			final Supplier<List<String>> selectedFlagsInfoProvider = () -> lArgs.stream()
					.filter(x -> x.startsWith("--")).collect(Collectors.toList());
			while (true) {
				String newFlag = readInput("Type flag here, you can just leave this empty and type Enter to finish",
						"there is no validation here so you have to type the flag correctly, otherwise error will be raised upon starting app",
						selectedFlagsInfoProvider, s -> {
							String flag = s.trim().toLowerCase();
							if (!flag.startsWith("--") || flag.length() < 3 || flag.startsWith("---"))
								return new Tuple3<>(false,
										"Wrong format, a flag must starts with '--', for example: '--help' (without quotes)",
										null);
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
		} else {
			info(ColorizeUtil.formatAsk, "FYI: command-line builder is available at: cb.bh99bot.com");
		}

		return lArgs.stream().distinct().collect(Collectors.toList()).toArray(new String[0]);
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

		// noinspection rawtypes
		for (@SuppressWarnings("rawtypes")
		FlagPattern flagPattern : parseArgumentsResult.usingFlags)
			if (!flagPattern.isSupportedByApp(instance)) {
				System.out.println(instance.getHelp());
				throw new InvalidFlagException(String.format("Flag '--%s' does not supported by '%s'",
						flagPattern.getName(), instance.getAppCode()));
			}

		try {
			MavenXpp3Reader reader = new MavenXpp3Reader();
			Model model = reader.read(new FileReader("pom.xml"));
			String version = model.getVersion();
			info(ColorizeUtil.formatAsk, "Hi, my name is %s v%s, have a nice day", botName, version);
			VersionUtil.setCurrentAppVersion(version);
		} catch (Exception ignored) {
			info(ColorizeUtil.formatAsk, "Hi, my name is %s, have a nice day", botName);
		}

		info(Cu.i().magenta("Please give me a Star").cyan(" at my github repository https://github.com/9-9-9-9/Bit-Heroes-bot ").magenta("thank you").reset().toString());
		info(ColorizeUtil.formatAsk, "Visit our repository often to update latest version");
		instance.run(parseArgumentsResult);
	}

	@SuppressWarnings("rawtypes")
	private static ParseArgumentsResult parseArguments(String[] args) throws InvalidFlagException {
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
		String cfgProfileName = null;
		ArrayList<Familiar> familiarToBribeWithGems = null;
		int mainLoopInterval = -1;
		for (FlagPattern flagPattern : usingFlagPatterns) {
			if (!flagPattern.isAllowParam())
				continue;

			if (flagPattern instanceof FlagExitAfterAmountOfSeconds) {
				exitAfter = ((FlagExitAfterAmountOfSeconds) flagPattern).parseParams().get(0);
				continue;
			}

			if (flagPattern instanceof FlagProfileName) {
				cfgProfileName = ((FlagProfileName) flagPattern).parseParams().get(0);
				continue;
			}

			if (flagPattern instanceof FlagBribe) {
				familiarToBribeWithGems = ((FlagBribe) flagPattern).parseParams();
				continue;
			}

			if (flagPattern instanceof FlagAlterLoopInterval) {
				mainLoopInterval = ((FlagAlterLoopInterval) flagPattern).parseParams().get(0);
				continue;
			}

			throw new NotImplementedException(
					String.format("Not implemented extracting param for flag '--%s' (Class: %s)", flagPattern.getName(),
							flagPattern.getClass().getSimpleName()));
		}
		
		if (familiarToBribeWithGems == null)
			familiarToBribeWithGems = new ArrayList<>();

		if (exitAfter > 0)
			info("Application will exit after %s", TimeUtil.niceTimeLong(exitAfter));

		// Validate flags
		for (FlagPattern flagPattern : usingFlagPatterns)
			if (!flagPattern.isSupportedOnCurrentOsPlatform())
				throw new InvalidFlagException(
						String.format("Flag '%s' is not supported on %s", flagPattern.getCode(), OS.name));

		ScreenResolutionProfile screenResolutionProfile = new ScreenResolutionProfile.Profile800x520();
		boolean isSteam = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagPlayOnSteam);
		boolean isWeb = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagPlayOnWeb);
		if (isSteam && isWeb) {
			err("Ambiguous flags! Can not use both flags at the same time:");
			err("  '--web' for controlling web-version BitHeroes");
			err("  '--steam' for controlling Steam-version BitHeroes");
			System.exit(EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
		}

		if (!isSteam && !isWeb) {
			if (OS.isWin) {
				isSteam = readInput("Steam or Web?\n\t1. Steam\n\t2.Web", null, s -> {
					try {
						int opt = Integer.parseInt(s.trim());
						if (opt == 1)
							return new Tuple3<>(true, null, true);
						if (opt == 2)
							return new Tuple3<>(true, null, false);
					} catch (NumberFormatException ex) {
						// ignored
					}
					return new Tuple3<>(false, "Wrong answer, must be <1> for Steam or <2> or Web", null);
				});
				isWeb = !isSteam;
			} else {
				isWeb = true;
			}
		}

		args = Arrays.stream(args).skip(1).filter(x -> !x.startsWith("--")).toArray(String[]::new);

		Class<? extends AbstractApplication> applicationClassFromAppCode = Configuration
				.getApplicationClassFromAppCode(appCode);

		if (applicationClassFromAppCode == null)
			throw new IllegalArgumentException("First argument must be a valid app code");

		ParseArgumentsResult li = new ParseArgumentsResult(applicationClassFromAppCode, args, usingFlagPatterns);
		li.steam = isSteam;
		li.web = isWeb;
		li.exitAfterXSecs = exitAfter;
		li.mainLoopInterval = mainLoopInterval;
		li.exitAfkIfWaitForResourceGeneration = usingFlagPatterns.stream()
				.anyMatch(x -> x instanceof FlagExitAfkAfterIfWaitResourceGeneration);
		li.shutdownAfterExit = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagShutdownAfterExit);
		li.closeGameWindowAfterExit = usingFlagPatterns.stream()
				.anyMatch(x -> x instanceof FlagCloseGameWindowAfterExit);
		li.displayHelp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagPrintHelpMessage);
		li.enableSavingDebugImages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagSaveDebugImages);
		li.enableDebugMessages = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagShowDebugMessages);
		li.disableTelegramNoti = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagMuteNoti);
		li.screenResolutionProfile = screenResolutionProfile;
		li.cfgProfileName = cfgProfileName;
		li.familiarToBribeWithGems = familiarToBribeWithGems;
		// events
		li.ePvp = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoPvp);
		li.eWorldBoss = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoWorldBoss);
		li.eRaid = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoRaid);
		li.eInvasion = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoInvasion);
		li.eExpedition = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoExpedition);
		li.eGvg = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoGvG);
		li.eTrials = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoTrials);
		li.eGauntlet = usingFlagPatterns.stream().anyMatch(x -> x instanceof FlagDoGauntlet);
		// end events
		return li;
	}

	private static BufferedReader bufferedReader = null;

	public static synchronized BufferedReader getBufferedReader() {
		if (bufferedReader == null)
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		return bufferedReader;
	}

	public static <T> T readInput(String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform) {
		return readInput(ask, desc, transform, false);
	}

	public static <T> T readInput(String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform,
			boolean allowBlankAndIfBlankThenReturnNull) {
		return readInput(ask, desc, null, transform, allowBlankAndIfBlankThenReturnNull);
	}

	public static boolean readYesNoInput(String ask, String desc) {
		return readYesNoInput(ask, desc, false);
	}

	public static boolean readYesNoInput(String ask, String desc, boolean emptyAsNo) {
		Boolean result = readInput(ask, desc == null ? "Answer by typing Y/N" : desc, s -> {
			String answer = s.trim().toLowerCase();
			if ("y".equals(answer) || "yes".equals(answer))
				return new Tuple3<>(true, null, true);
			if ("n".equals(answer) || "no".equals(answer))
				return new Tuple3<>(true, null, false);
			return new Tuple3<>(false, "Not a valid answer", null);
		}, emptyAsNo);
		if (result == null)
			return false;
		return result;
	}

	public static <T> T readInput(String ask, String desc, Supplier<List<String>> selectedOptionsInfoProvider,
			Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
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
	public static final int EXIT_CODE_REQUIRE_SUDO = 12;
	public static final int EXIT_CODE_VERSION_IS_REJECTED = 13;
	public static final int EXIT_CODE_UNHANDLED_EXCEPTION = -1;
}
