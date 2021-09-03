package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static bh.bot.common.Log.info;

@AppCode(code = "setting")
public class SettingApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try (
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
        ) {
            int profileNumber = readInput(br, "Which profile do you want to edit?", String.format("select a number, min 1, max %d", GenMiniClient.supportMaximumNumberOfAccounts), new Function<String, Tuple3<Boolean, String, Integer>>() {
                @Override
                public Tuple3<Boolean, String, Integer> apply(String s) {
                    try {
                        int num = Integer.parseInt(s.trim());
                        if (num >= 1 && num <= GenMiniClient.supportMaximumNumberOfAccounts)
                            return new Tuple3<>(true, null, num);
                        return new Tuple3<>(false, "Value must be in range from 1 to " + GenMiniClient.supportMaximumNumberOfAccounts, 0);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, "Not a number", 0);
                    }
                }
            });

            String fileName = Configuration.getProfileConfigFileName(profileNumber);
            File file = new File(fileName);
            if (file.exists() && file.isDirectory())
                throw new InvalidDataException("%s is a directory", fileName);
            Tuple2<Boolean, Configuration.UserConfig> resultLoadUserConfig = Configuration.loadUserConfig(profileNumber);
            int raidLevel, raidMode, worldBossLevel;
            if (resultLoadUserConfig._1) {
                raidLevel = resultLoadUserConfig._2.raidLevel;
                raidMode = resultLoadUserConfig._2.raidMode;
                worldBossLevel = resultLoadUserConfig._2.worldBossLevel;

                if (resultLoadUserConfig._2.isValidRaidLevel())
                    info("Selected Raid level %s", resultLoadUserConfig._2.getRaidLevelDesc());
                else
                    info("You haven't specified Raid level");

                if (Configuration.UserConfig.isValidDifficultyMode(resultLoadUserConfig._2.raidMode))
                    info("Selected Raid mode %s", resultLoadUserConfig._2.getRaidModeDesc());
                else
                    info("You haven't specified Raid mode (Normal/Hard/Heroic)");

                if (resultLoadUserConfig._2.isValidWorldBossLevel())
                    info("Selected World Boss %s", resultLoadUserConfig._2.getWorldBossLevelDesc());
                else
                    info("You haven't specified World Boss level");
            } else {
                raidLevel = 0;
                raidMode = 0;
                worldBossLevel = 0;
            }

            //
            final Tuple2<Byte, Byte> raidLevelRange = Configuration.UserConfig.getRaidLevelRange();
            StringBuilder sb = new StringBuilder();
            sb.append("All Raid levels:\n");
            for (int rl = raidLevelRange._1; rl <= raidLevelRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, Configuration.UserConfig.getRaidLevelDesc(rl)));
            sb.append("Specific Raid level?");
            Integer tmp = readInput(br, sb.toString(), "See the list above. To skip and keep the current value, just leave this empty and press Enter", new Function<String, Tuple3<Boolean, String, Integer>>() {
                @Override
                public Tuple3<Boolean, String, Integer> apply(String s) {
                    try {
                        int num = Integer.parseInt(s);
                        if (num >= raidLevelRange._1 && num <= raidLevelRange._2)
                            return new Tuple3<>(true, null, num);
                        return new Tuple3<>(false, String.format("Value must in range from %d to %d", raidLevelRange._1, raidLevelRange._2), 0);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, String.format("Must be a number in range from %d to %d", raidLevelRange._1, raidLevelRange._2), 0);
                    }
                }
            }, true);
            raidLevel = tmp == null ? raidLevel : tmp.intValue();
            //
            Tuple2<Byte, Byte> modeRange = Configuration.UserConfig.getModeRange();
            info("All Raid's difficulty mode:");
            for (byte rl = modeRange._1; rl <= modeRange._2; rl++)
                info("%2d. %s", rl, Configuration.UserConfig.getDifficultyModeDesc(rl, "Raid"));
            tmp = readInput(br, "Specific Raid mode?", "See the list above. To skip and keep the current value, just leave this empty and press Enter", new Function<String, Tuple3<Boolean, String, Integer>>() {
                @Override
                public Tuple3<Boolean, String, Integer> apply(String s) {
                    try {
                        int num = Integer.parseInt(s);
                        if (num >= modeRange._1 && num <= modeRange._2)
                            return new Tuple3<>(true, null, num);
                        return new Tuple3<>(false, String.format("Value must in range from %d to %d", modeRange._1, modeRange._2), 0);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, String.format("Must be a number in range from %d to %d", modeRange._1, modeRange._2), 0);
                    }
                }
            }, true);
            raidMode = tmp == null ? raidMode : tmp.intValue();

            //
            final Tuple2<Byte, Byte> woldBossLevelRange = Configuration.UserConfig.getWorldBossLevelRange();
            info("All World Boss levels:");
            for (int rl = woldBossLevelRange._1; rl <= woldBossLevelRange._2; rl++)
                info("%2d. %s", rl, Configuration.UserConfig.getWorldBossLevelDesc(rl));
            tmp = readInput(br, "Specific World Boss level?", "See the list above. To skip and keep the current value, just leave this empty and press Enter", new Function<String, Tuple3<Boolean, String, Integer>>() {
                @Override
                public Tuple3<Boolean, String, Integer> apply(String s) {
                    try {
                        int num = Integer.parseInt(s);
                        if (num >= woldBossLevelRange._1 && num <= woldBossLevelRange._2)
                            return new Tuple3<>(true, null, num);
                        return new Tuple3<>(false, String.format("Value must in range from %d to %d", woldBossLevelRange._1, woldBossLevelRange._2), 0);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, String.format("Must be a number in range from %d to %d", woldBossLevelRange._1, woldBossLevelRange._2), 0);
                    }
                }
            }, true);
            worldBossLevel = tmp == null ? worldBossLevel : tmp.intValue();
            //

            sb = new StringBuilder();
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.raidLevelKey, raidLevel));
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.raidModeKey, raidMode));
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.worldBossLevelKey, worldBossLevel));

            info("You have selected:");
            info("  %s mode of raid %s", Configuration.UserConfig.getDifficultyModeDesc((byte)raidMode, "Raid"), Configuration.UserConfig.getRaidLevelDesc((byte)raidLevel));
            info("  world boss %s", Configuration.UserConfig.getWorldBossLevelDesc((byte)worldBossLevel));
            boolean save = readInput(br, "Do you want to save the above setting into profile number " + profileNumber + "?", "Press Y/N then enter", new Function<String, Tuple3<Boolean, String, Boolean>>() {
                @Override
                public Tuple3<Boolean, String, Boolean> apply(String s) {
                    s = s.trim().toLowerCase();
                    if (s.equals("y"))
                        return new Tuple3<>(true, null, true);
                    if (s.equals("n"))
                        return new Tuple3<>(true, null, false);
                    return new Tuple3<>(false, "Must be 'Y' or 'N'", false);
                }
            });

            if (save) {
                Files.write(Paths.get(fileName), sb.toString().getBytes());
                info("Saved successfully");
            } else {
                info("Nothing was changed");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
        }
    }

    @Override
    protected String getAppName() {
        return "Setting";
    }

    @Override
    protected String getScriptFileName() {
        return "setting";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Do setting raid level, raid mode,...";
    }

    @Override
    protected String getLimitationExplain() {
        return "This is an utility for setting purpose only";
    }

    @Override
    protected boolean isRequiredToLoadImages() {
        return false;
    }
}
