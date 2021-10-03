package bh.bot.app;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.StringUtil.isNotBlank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.OS;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.Extensions;

@AppMeta(code = "client", name = "Generate mini-client", displayOrder = 4)
public class GenMiniClient extends AbstractApplication {
    private static final String requireChromeUserDirName = "chrome-user-dir";

    public static final int supportMaximumNumberOfAccounts = 10;
    private static final String keyChromePath = "external.application.chrome.path";
    private static final String keyChromeUserDirPath = "external.mini-client.user.dir";

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    protected void internalRun(String[] args) {

        ArrayList<GameAccount> gameAccounts;
        try {
            gameAccounts = getGameAccountsConfig();

            if (gameAccounts.size() < 1)
                throw new InvalidDataException("None data was provided");

        } catch (Exception ex) {
            err("Unable to generate mini-client!!!\n  Reason: %s", ex.getMessage());
            info(ColorizeUtil.formatInfo, "To be able to use mini game client (using Google Chrome), the following conditions must be met:");
            info(" 1. Google Chrome must be installed");
            info(" 2. You can play Bit Heroes game at https://www.kongregate.com/games/Juppiomenz/bit-heroes");
            info(" 3. Play Bit Heroes in Web using Chrome and press F12 to open Dev Tools");
            info(" 4. Go to Console tab");
            info(" 5. Paste the content of file 'prepare-mini-chrome-client.txt' into console tab");
            info(" 6. Copy the output lines and override corresponding values in user-config.properties");
            info(" 7. Run the script '%s'  again", Extensions.scriptFileName("client"));
            info(ColorizeUtil.formatInfo, "Notes: it's able to generate more than one client, just by modify the '1.' prefix of the keys, support up to maximum %d accounts", supportMaximumNumberOfAccounts);
            System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            return;
        }

        info("Current OS: %s", OS.name);
        info("To run mini-client, you must have Google Chrome installed");

        File dir = new File("bh-client");
        if (!dir.exists())
            //noinspection ResultOfMethodCallIgnored
            dir.mkdir();

        final String scriptNamePrefix = "mini-game-on-chrome";
        final String scriptExtension = OS.isWin ? "bat" : "sh";
        try (
                InputStream fileGame = Configuration.class.getResourceAsStream("/templates/game.html");
                InputStream fileHolo = Configuration.class.getResourceAsStream("/game-scripts/holodeck_javascripts.js");
                InputStream fileSiteWide = Configuration.class.getResourceAsStream("/game-scripts/sitewide_javascripts.js")
        ) {
            String originalGameHtml = readFromInputStream(fileGame);
            Files.write(Paths.get("bh-client/holodeck_javascripts.js"), readFromInputStream(fileHolo).getBytes());
            Files.write(Paths.get("bh-client/sitewide_javascripts.js"), readFromInputStream(fileSiteWide).getBytes());

            final String chromeUserDir = getChromeUserDir();
            if (chromeUserDir == null) {
                System.exit(Main.EXIT_CODE_EXTERNAL_REASON);
                return;
            }

            info(ColorizeUtil.formatWarning, "Chrome user directory: %s", chromeUserDir);

            String chromePathOnWindows = getChromePathOnWindows();

            for (GameAccount gameAccount : gameAccounts) {
                String html = originalGameHtml
                        .replaceFirst("%game.kong.user.id%", String.valueOf(gameAccount.kongUserId))
                        .replaceFirst("%game.kong.user.name%", String.valueOf(gameAccount.kongUserName))
                        .replaceFirst("%game.auth.token%", String.valueOf(gameAccount.authToken));

                Path pathIndex = Paths.get(String.format("bh-client/index%d.html", gameAccount.fromPrefix));
                Files.write(pathIndex, html.getBytes());

                String app;
                List<String> chromeArgs = new ArrayList<>();
                if (OS.isMac) {
                    app = "open";
                    chromeArgs.add("-a");
                    chromeArgs.add("\"Google Chrome\"");
                    chromeArgs.add("--args");
                    // chromeArgs.add(String.format("\"--user-data-dir=%s\"", chromeUserDir.getAbsolutePath()));
                    chromeArgs.add("--window-size=805,545");
                    chromeArgs.add("--window-position=0,0");
                    chromeArgs.add(String.format("\"--app=file://%s\"", pathIndex.toAbsolutePath().toString()));
                } else if (OS.isWin) {
                    app = String.format("\"%s\"", chromePathOnWindows);
                    chromeArgs.add(String.format("\"--user-data-dir=%s\"", chromeUserDir));
                    chromeArgs.add("--window-size=820,565");
                    chromeArgs.add("--window-position=0,0");
                    chromeArgs.add(String.format("\"--app=file://%s\"", pathIndex.toAbsolutePath().toString()));
                } else {
                    app = "google-chrome";
                    chromeArgs.add(String.format("'--user-data-dir=%s'", chromeUserDir));
                    chromeArgs.add("--window-size=800,520");
                    chromeArgs.add("--window-position=0,0");
                    chromeArgs.add(String.format("'--app=file://%s'", pathIndex.toAbsolutePath().toString()));
                }

                StringBuilder sb = new StringBuilder();
                if (OS.isMac) {
                    sb.append("#!/usr/bin/env bash");
                    sb.append('\n');
                    sb.append("read -p \"Launching this mini client will force close all current Google Chrome processes! Are you sure? (Y/N) \" -n 1 -r");
                    sb.append('\n');
                    sb.append("echo");
                    sb.append('\n');
                    sb.append("if [[ ! $REPLY =~ ^[Yy]$ ]]");
                    sb.append('\n');
                    sb.append("then");
                    sb.append('\n');
                    sb.append("  echo \"You didn't accept so mini client can not be launched\"");
                    sb.append('\n');
                    sb.append("  exit 1");
                    sb.append('\n');
                    sb.append("fi");
                    sb.append('\n');
                    sb.append('\n');
                    sb.append("pkill -a -i \"Google Chrome\"");
                    sb.append('\n');
                    sb.append("sleep 2s");
                    sb.append('\n');
                } else if (OS.isLinux) {
                    sb.append("#!/bin/bash");
                }

                sb.append('\n');
                sb.append(app);
                for (String arg : chromeArgs) {
                    sb.append(' ');
                    sb.append(arg);
                }

                if (OS.isMac || OS.isLinux) {
                    sb.append(" > /dev/null 2>&1&");
                }

                String scriptFileName = String.format("%s%d.%s", scriptNamePrefix, gameAccount.fromPrefix, scriptExtension);
                Files.write(Paths.get(scriptFileName), sb.toString().getBytes());
                info("Generated file '%s' for account %s", scriptFileName, gameAccount.kongUserName);
            }

            if (OS.isMac || OS.isLinux) {
                info("Now you can launch mini game client by running the following script in terminal:");
                info(" bash ./%s*.%s", scriptNamePrefix, scriptExtension);
            } else if (OS.isWin) {
                info("Now you can launch mini game client by double click the '%s*.%s' file", scriptNamePrefix, scriptExtension);
            }
        } catch (Exception e) {
            e.printStackTrace();
            err("Unable to generate mini-client");
        }
    }

    private ArrayList<GameAccount> getGameAccountsConfig() {
        ArrayList<GameAccount> result = new ArrayList<>();
        for (int no = 1; no <= supportMaximumNumberOfAccounts; no++) {
            final String keyId = Configuration.read(no + ".game.kong.user.id");
            final String keyName = Configuration.read(no + ".game.kong.user.name");
            final String keyToken = Configuration.read(no + ".game.auth.token");
            if (isBlank(keyId) && isBlank(keyName) && isBlank(keyToken)) {
                continue;
            }
            if (isNotBlank(keyId) && isNotBlank(keyName) && isNotBlank(keyToken)) {
                int id;
                try {
                    id = Integer.parseInt(keyId);
                } catch (NumberFormatException ex) {
                    throw new InvalidDataException("Problem with key '%d.game.kong.user.id': is not a valid number", no);
                }
                result.add(new GameAccount(no, id, keyName.trim(), keyToken.trim()));
                continue;
            }
            throw new InvalidDataException("Problem with keys with prefix '%d.', missing one or two of three required keys", no);
        }
        return result;
    }

    private String getChromeUserDir() {
        final String defaultChromeUserDir = requireChromeUserDirName;
        try {
            String cfgChromeUserDir = Configuration.read(keyChromeUserDirPath);
            if (isBlank(cfgChromeUserDir)) {
                info(ColorizeUtil.formatInfo, "FYI: you can specify a chrome user's directory so next time when you download a new version of this Bit Heroes bot, you don't need to copy the '%s' folder", defaultChromeUserDir);
                info(ColorizeUtil.formatInfo, "To do it, edit file `user-config.properties` and set path to key `%s`", keyChromeUserDirPath);
                return new File(defaultChromeUserDir).getAbsolutePath();
            }

            File dir = new File(cfgChromeUserDir);
            if (dir.exists()) {
                if (!dir.isDirectory()) {
                    err("The directory you have specified by the value of key %s in user-config.properties is NOT a directory", keyChromeUserDirPath);
                    err("%s is a file, it's NOT a directory. Please specific an empty directory instead", dir.getAbsolutePath());
                    return null;
                }

                if (dir.getName().equalsIgnoreCase(requireChromeUserDirName)) {
                    return dir.getAbsolutePath();
                }

                return appendRequiredDirNamePart(dir);
            }

            if (dir.getName().equalsIgnoreCase(requireChromeUserDirName)) {
                if (!dir.mkdir()) {
                    err("Unable to create directory %s", dir.getAbsolutePath());
                    err("May be related to permission issue, please specify another directory to the key '%s' in the user-config.properties file", keyChromeUserDirPath);
                    return null;
                }
                return dir.getAbsolutePath();
            }

            return appendRequiredDirNamePart(dir);
        } catch (Exception ex) {
            ex.printStackTrace();
            err("An error occurs while trying to get chrome user dir based on configuration of key `%s` in user-config.properties file", keyChromeUserDirPath);
            String result = new File(defaultChromeUserDir).getAbsolutePath();
            err("The following directory will be used by default: %s", result);
            return result;
        }
    }

    private String appendRequiredDirNamePart(File dir) {
        warn("Custom chrome's user directory must ends with '%s', this folder name will be automatically appended", requireChromeUserDirName);
        dir = Paths.get(dir.getAbsolutePath(), requireChromeUserDirName).toFile();
        warn("Directory after appended: %s", dir.getAbsolutePath());
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                err("But it's NOT a directory, please specify another directory to the key '%s' in the user-config.properties file", keyChromeUserDirPath);
                return null;
            }
        } else {
            if (!dir.mkdir()) {
                err("Unable to create that directory, may be related to permission issue, please specify another directory to the key '%s' in the user-config.properties file", keyChromeUserDirPath);
                return null;
            }
        }
        return dir.getAbsolutePath();
    }

    private String getChromePathOnWindows() {
        if (!OS.isWin)
            return null;
        final String defaultChromePath = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
        String chromePathOnWindows = Configuration.read(keyChromePath);
        if (isBlank(chromePathOnWindows)) {
            info("Missing configuration of Google Chrome path (key %s), going to set to use default path: %s", keyChromePath, defaultChromePath);
            chromePathOnWindows = defaultChromePath;
        }

        if (!(new File(chromePathOnWindows)).exists()) {
            err("ERROR: Chrome not found");
            info("Can not find Google Chrome at provided path %s", chromePathOnWindows);
            info("Please provide a correct path to chrome.exe into key '%s' on user-config.properties file", keyChromePath);
            info("To get it, you can do this:");
            info("1. Right click on Google Chrome shortcut");
            info("2. Select Properties");
            info("3. Copy value of Target line");
            info("4. Transform the value to a correct format. For example:");
            info("  - Input value is:");
            info("    \"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe\"");
            info("    (with the double quotes)");
            info("  - Should be translated into:");
            info("    C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe");
            info("    (remove the double quotes and double the back slashes)");
            info("5. Fill the translated path into key '%s' of the user-config.properties file", keyChromePath);
            info("    Example:");
            info("%s=C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe", keyChromePath);
            info("6. Save the file user-config.properties after modified");
            info("7. Run script '.\\client.bat' to generate mini-client");
            System.exit(Main.EXIT_CODE_EXTERNAL_REASON);
        }
        return chromePathOnWindows;
    }

    public static class GameAccount {
        public final int fromPrefix;
        public final int kongUserId;
        public final String kongUserName;
        public final String authToken;

        public GameAccount(int fromPrefix, int kongUserId, String kongUserName, String authToken) {
            this.fromPrefix = fromPrefix;
            this.kongUserId = kongUserId;
            this.kongUserName = kongUserName;
            this.authToken = authToken;
        }
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Generate mini client based on configuration, allow you to play in special Google Chrome window";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    @Override
    protected boolean isRequiredToLoadImages() {
        return false;
    }
    
    @Override
    protected boolean skipCheckVersion() {
    	return true;
    }

    @Override
    protected boolean requireSpecificClientType() {
        return false;
    }
}
