package bh.bot.app;

import bh.bot.common.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

public class GenMiniClient extends AbstractApplication {
    private static final File chromeUserDir = new File("chrome-user-dir");

    @Override
    protected void internalRun(String[] args) {
        throwNotSupportedFlagExit(exitAfterXSecs);

        String errMsg = Configuration.loadGameCfg();
        String scriptFileName = String.format("mini-game-on-chrome.%s", Configuration.OS.isWin ? "bat" : "sh");
        if (errMsg != null)
        {
            err("Unable to generate %s with error:", scriptFileName);
            err("  %s", errMsg);
            info("To be able to use mini game client (using Google Chrome), the following conditions must be met:");
            info(" 1. Google Chrome must be installed");
            info(" 2. You can play Bit Heroes game at https://www.kongregate.com/games/Juppiomenz/bit-heroes");
            info(" 3. Play Bit Heroes in Web using Chrome and press F12 to open Dev Tools");
            info(" 4. Go to Console tab");
            info(" 5. Paste the content of file 'prepare-mini-chrome-client.txt' into console tab");
            info(" 6. Copy the output lines and override corresponding values in config.properties");
            info(" 7. Run the script 'build.%s'  again", Configuration.OS.isWin ? "bat" : "sh");
            return;
        }

        try (
                InputStream fileGame = Configuration.class.getResourceAsStream("/templates/game.html");
                InputStream fileHolo = Configuration.class.getResourceAsStream("/game-scripts/holodeck_javascripts.js");
                InputStream fileSiteWide = Configuration.class.getResourceAsStream("/game-scripts/sitewide_javascripts.js");
        ) {
            String html = readFromInputStream(fileGame)
                    .replaceFirst("%game.kong.user.id%", String.valueOf(Configuration.Game.kongUserId))
                    .replaceFirst("%game.kong.user.name%", String.valueOf(Configuration.Game.kongUserName))
                    .replaceFirst("%game.auth.token%", String.valueOf(Configuration.Game.authToken));

            File dir = new File("bh-client");
            if (!dir.exists())
                dir.mkdir();

            Path pathIndex = Paths.get("bh-client/index.html");
            Files.write(pathIndex, html.getBytes());
            Files.write(Paths.get("bh-client/holodeck_javascripts.js"), readFromInputStream(fileHolo).getBytes());
            Files.write(Paths.get("bh-client/sitewide_javascripts.js"), readFromInputStream(fileSiteWide).getBytes());

            info("To run mini game client using file '%s', you must have Google Chrome installed", scriptFileName);
            info("Current OS: %s", Configuration.OS.name);
            String app;
            List<String> chromeArgs = new ArrayList<>();
            if (Configuration.OS.isMac) {
                app = "open";
                chromeArgs.add("-a");
                chromeArgs.add("\"Google Chrome\"");
                chromeArgs.add("--args");
                // chromeArgs.add(String.format("\"--user-data-dir=%s\"", chromeUserDir.getAbsolutePath()));
                chromeArgs.add("--window-size=805,545");
                chromeArgs.add("--window-position=0,0");
                chromeArgs.add(String.format("\"--app=file://%s\"", pathIndex.toAbsolutePath().toString()));
            } else if (Configuration.OS.isWin) {
                app = "\"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe\"";
				chromeArgs.add(String.format("\"--user-data-dir=%s\"", chromeUserDir.getAbsolutePath()));
				chromeArgs.add("--window-size=820,565");
				chromeArgs.add("--window-position=0,0");
				chromeArgs.add(String.format("\"--app=file://%s\"", pathIndex.toAbsolutePath().toString()));
            } else {
                app = "google-chrome";
				chromeArgs.add(String.format("'--user-data-dir=%s'", chromeUserDir.getAbsolutePath()));
				chromeArgs.add("--window-size=800,520");
				chromeArgs.add("--window-position=0,0");
				chromeArgs.add(String.format("'--app=file://%s'", pathIndex.toAbsolutePath().toString()));
            }

            StringBuffer sb = new StringBuffer();
            if (Configuration.OS.isMac) {
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
            } else if (Configuration.OS.isUnix) {
                sb.append("#!/bin/bash");
            }

            sb.append('\n');
            sb.append(app);
            for (String arg : chromeArgs) {
                sb.append(' ');
                sb.append(arg);
            }

            if (Configuration.OS.isMac || Configuration.OS.isUnix) {
                sb.append(" > /dev/null 2>&1&");
            }

            Files.write(Paths.get(scriptFileName), sb.toString().getBytes());

            if (Configuration.OS.isMac || Configuration.OS.isUnix) {
                info("Now you can launch mini game client by running the following script in terminal:");
                info (" bash ./%s", scriptFileName);
                info("Or even more simpler:");
                info (" ./mini");
                info("(it is a shortcut which was generated during execution of build.sh)");
            } else if (Configuration.OS.isWin) {
                info("Now you can launch mini game client by double click the '%s' file", scriptFileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAppCode() {
        return "client";
    }

    @Override
    protected String getAppName() {
        return "Bit Heroes on Chrome client";
    }

    @Override
    protected String getScriptFileName() {
        return "client";
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
    protected String getFlags() {
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
}
