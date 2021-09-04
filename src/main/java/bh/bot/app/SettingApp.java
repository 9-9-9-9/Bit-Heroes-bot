package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static bh.bot.Main.colorFormatInfo;
import static bh.bot.common.Log.info;

@AppCode(code = "setting")
public class SettingApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try {
            int profileNumber = readProfileNumber("Which profile do you want to edit?");

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
                    info(colorFormatInfo, "Selected Raid level %s", resultLoadUserConfig._2.getRaidLevelDesc());
                else
                    info(colorFormatInfo, "You haven't specified Raid level");

                if (Configuration.UserConfig.isValidDifficultyMode(resultLoadUserConfig._2.raidMode))
                    info(colorFormatInfo, "Selected Raid mode %s", resultLoadUserConfig._2.getRaidModeDesc());
                else
                    info(colorFormatInfo, "You haven't specified Raid mode (Normal/Hard/Heroic)");

                if (resultLoadUserConfig._2.isValidWorldBossLevel())
                    info(colorFormatInfo, "Selected World Boss %s", resultLoadUserConfig._2.getWorldBossLevelDesc());
                else
                    info(colorFormatInfo, "You haven't specified World Boss level");

                info("Press any key to continue...");
                Main.getBufferedReader().readLine();
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
            Integer tmp = readInput(sb.toString(), raidLevelRange._1, raidLevelRange._2);
            raidLevel = tmp == null ? raidLevel : tmp.intValue();
            //
            Tuple2<Byte, Byte> modeRange = Configuration.UserConfig.getModeRange();
            sb = new StringBuilder("All Raid's difficulty mode:\n");
            for (byte rl = modeRange._1; rl <= modeRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, Configuration.UserConfig.getDifficultyModeDesc(rl, "Raid")));
            sb.append("Specific Raid mode?");
            tmp = readInput(sb.toString(), modeRange._1, modeRange._2);
            raidMode = tmp == null ? raidMode : tmp.intValue();

            //
            final Tuple2<Byte, Byte> woldBossLevelRange = Configuration.UserConfig.getWorldBossLevelRange();
            sb = new StringBuilder("All World Boss levels:\n");
            for (int rl = woldBossLevelRange._1; rl <= woldBossLevelRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, Configuration.UserConfig.getWorldBossLevelDesc(rl)));
            sb.append("Specific World Boss level?");
            tmp = readInput(sb.toString(), woldBossLevelRange._1, woldBossLevelRange._2);
            worldBossLevel = tmp == null ? worldBossLevel : tmp.intValue();
            //

            sb = new StringBuilder();
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.raidLevelKey, raidLevel));
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.raidModeKey, raidMode));
            sb.append(String.format("%s=%d\n", Configuration.UserConfig.worldBossLevelKey, worldBossLevel));

            Configuration.UserConfig newCfg = new Configuration.UserConfig(profileNumber, (byte) raidLevel, (byte) raidMode, (byte) worldBossLevel);
            info(colorFormatInfo, "Your setting:");
            if (newCfg.isValidRaidLevel() && newCfg.isValidDifficultyMode(newCfg.raidMode))
                info(colorFormatInfo, "  %s mode of raid %s", Configuration.UserConfig.getDifficultyModeDesc((byte) raidMode, "Raid"), Configuration.UserConfig.getRaidLevelDesc((byte) raidLevel));
            else
                info(colorFormatInfo, "  raid has not been set");
            if (newCfg.isValidWorldBossLevel() && newCfg.isValidDifficultyMode(newCfg.raidMode))
                info(colorFormatInfo, "  world boss %s", Configuration.UserConfig.getWorldBossLevelDesc((byte) worldBossLevel));
            else
                info(colorFormatInfo, "  world boss has not been set");
            boolean save = readInput("Do you want to save the above setting into profile number " + profileNumber + "?", "Press Y/N then enter", s -> {
                s = s.trim().toLowerCase();
                if (s.equals("y"))
                    return new Tuple3<>(true, null, true);
                if (s.equals("n"))
                    return new Tuple3<>(true, null, false);
                return new Tuple3<>(false, "Must be 'Y' or 'N'", false);
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

    private Integer readInput(String ask, int min, int max) {
        return readInput(ask, "See the list above. To skip and keep the current value, just leave this empty and press Enter", s -> {
            try {
                int num = Integer.parseInt(s);
                if (num >= min && num <= max)
                    return new Tuple3<>(true, null, num);
                return new Tuple3<>(false, String.format("Value must in range from %d to %d", min, max), 0);
            } catch (NumberFormatException ex) {
                return new Tuple3<>(false, String.format("Must be a number in range from %d to %d", min, max), 0);
            }
        }, true);
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
