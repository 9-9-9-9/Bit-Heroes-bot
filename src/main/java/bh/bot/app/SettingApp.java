package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.info;

@AppMeta(code = "setting", name = "Setting", displayOrder = 5)
public class SettingApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try {
            String cfgProfileName = readCfgProfileName("Which profile do you want to edit?", "You can select an existing profile to edit or specify a new profile by typing a new name");

            String fileName = Configuration.getProfileConfigFileName(cfgProfileName);
            File file = new File(fileName);
            if (file.exists() && file.isDirectory())
                throw new InvalidDataException("%s is a directory", fileName);
            Tuple2<Boolean, UserConfig> resultLoadUserConfig = Configuration.loadUserConfig(cfgProfileName);
            int raidLevel, raidMode, worldBossLevel, expeditionPlace;
            if (resultLoadUserConfig._1) {
                raidLevel = resultLoadUserConfig._2.raidLevel;
                raidMode = resultLoadUserConfig._2.raidMode;
                worldBossLevel = resultLoadUserConfig._2.worldBossLevel;
                expeditionPlace = resultLoadUserConfig._2.expeditionPlace;

                if (resultLoadUserConfig._2.isValidRaidLevel())
                    info(ColorizeUtil.formatInfo, "Selected Raid level %s", resultLoadUserConfig._2.getRaidLevelDesc());
                else
                    info(ColorizeUtil.formatInfo, "You haven't specified Raid level");

                if (UserConfig.isValidDifficultyMode(resultLoadUserConfig._2.raidMode))
                    info(ColorizeUtil.formatInfo, "Selected Raid mode %s", resultLoadUserConfig._2.getRaidModeDesc());
                else
                    info(ColorizeUtil.formatInfo, "You haven't specified Raid mode (Normal/Hard/Heroic)");

                if (resultLoadUserConfig._2.isValidWorldBossLevel())
                    info(ColorizeUtil.formatInfo, "Selected World Boss %s", resultLoadUserConfig._2.getWorldBossLevelDesc());
                else
                    info(ColorizeUtil.formatInfo, "You haven't specified World Boss level");

                if (resultLoadUserConfig._2.isValidExpeditionPlace())
                    info(ColorizeUtil.formatInfo, "Selected Expedition door %s", resultLoadUserConfig._2.getExpeditionPlaceDesc());
                else
                    info(ColorizeUtil.formatInfo, "You haven't specified Expedition door");

                info("Press any key to continue...");
                Main.getBufferedReader().readLine();
            } else {
                raidLevel = 0;
                raidMode = 0;
                worldBossLevel = 0;
                expeditionPlace = 0;
            }

            //
            final Tuple2<Byte, Byte> raidLevelRange = UserConfig.getRaidLevelRange();
            StringBuilder sb = new StringBuilder();
            sb.append("All Raid levels:\n");
            for (int rl = raidLevelRange._1; rl <= raidLevelRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, UserConfig.getRaidLevelDesc(rl)));
            sb.append("Specific Raid level?");
            Integer tmp = readIntInput(sb.toString(), raidLevelRange._1, raidLevelRange._2);
            raidLevel = tmp == null ? raidLevel : tmp;
            //
            Tuple2<Byte, Byte> modeRange = UserConfig.getModeRange();
            sb = new StringBuilder("All Raid's difficulty mode:\n");
            for (byte rl = modeRange._1; rl <= modeRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, UserConfig.getDifficultyModeDesc(rl, "Raid")));
            sb.append("Specific Raid mode?");
            tmp = readIntInput(sb.toString(), modeRange._1, modeRange._2);
            raidMode = tmp == null ? raidMode : tmp;
            //
            final Tuple2<Byte, Byte> woldBossLevelRange = UserConfig.getWorldBossLevelRange();
            sb = new StringBuilder("All World Boss levels:\n");
            for (int rl = woldBossLevelRange._1; rl <= woldBossLevelRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, UserConfig.getWorldBossLevelDesc(rl)));
            sb.append("Specific World Boss level?");
            tmp = readIntInput(sb.toString(), woldBossLevelRange._1, woldBossLevelRange._2);
            worldBossLevel = tmp == null ? worldBossLevel : tmp;
            //
            final Tuple2<Byte, Byte> expeditionPlaceRange = UserConfig.getExpeditionPlaceRange();
            sb = new StringBuilder("All Expedition doors:\n");
            sb.append(String.format("  %2d. %s\n", 0, "Unset (select every time)"));
            for (int rl = expeditionPlaceRange._1; rl <= expeditionPlaceRange._2; rl++)
                sb.append(String.format("  %2d. %s\n", rl, UserConfig.getExpeditionPlaceDesc(rl)));
            sb.append("Specific Expedition door to enter?");
            tmp = readIntInput(sb.toString(), 0, expeditionPlaceRange._2);
            expeditionPlace = tmp == null ? expeditionPlace : tmp;
            //
            UserConfig newCfg = new UserConfig(cfgProfileName, (byte) raidLevel, (byte) raidMode, (byte) worldBossLevel, (byte) expeditionPlace);

            sb = new StringBuilder("Your setting:\n");
            if (newCfg.isValidRaidLevel() && UserConfig.isValidDifficultyMode(newCfg.raidMode))
                sb.append(String.format("  %s mode of raid %s", UserConfig.getDifficultyModeDesc((byte) raidMode, "Raid"), UserConfig.getRaidLevelDesc((byte) raidLevel)));
            else
                sb.append("  raid has not been set");
            sb.append('\n');
            if (newCfg.isValidWorldBossLevel())
                sb.append(String.format("  world boss %s", UserConfig.getWorldBossLevelDesc((byte) worldBossLevel)));
            else
                sb.append("  world boss has not been set");
            sb.append('\n');
            if (newCfg.isValidExpeditionPlace())
                sb.append(String.format("  expedition %s", UserConfig.getExpeditionPlaceDesc((byte) expeditionPlace)));
            else
                sb.append("  expedition door has not been set");
            sb.append('\n');
            sb.append(String.format("Do you want to save the above setting into profile name '%s' ?", cfgProfileName));
            boolean save = readInput(sb.toString(), "Press Y/N then enter", s -> {
                s = s.trim().toLowerCase();
                if (s.equals("y"))
                    return new Tuple3<>(true, null, true);
                if (s.equals("n"))
                    return new Tuple3<>(true, null, false);
                return new Tuple3<>(false, "Must be 'Y' or 'N'", false);
            });

            if (save) {
                sb = new StringBuilder();
                sb.append(String.format("%s=%d\n", UserConfig.raidLevelKey, raidLevel));
                sb.append(String.format("%s=%d\n", UserConfig.raidModeKey, raidMode));
                sb.append(String.format("%s=%d\n", UserConfig.worldBossLevelKey, worldBossLevel));
                sb.append(String.format("%s=%d\n", UserConfig.expeditionPlaceKey, expeditionPlace));
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

    private Integer readIntInput(String ask, int min, int max) {
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
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Do setting raid level, raid mode, world boss level, expedition door to enter,...";
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
