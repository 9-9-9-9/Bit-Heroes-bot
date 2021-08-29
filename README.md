## This is bot for Bit Heroes
##### on Linux / Windows / ~~MacOS~~
[![Github Open Issues](https://img.shields.io/github/issues/9-9-9-9/Bit-Heroes-bot.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/issues)
[![Github All Releases](https://img.shields.io/github/downloads/9-9-9-9/Bit-Heroes-bot/total.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/releases)

have inspiration from https://github.com/tiemonl/Bit-Heroes-Fishing-Bot

**If you like my bot, please give this repo a Star, thanks**
___
[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for developers

Normal users please [read at this page](https://github.com/9-9-9-9/Bit-Heroes-bot/blob/master/README.release.md)

[_There're some notes for MacOS users, please read here_](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Notes-for-MacOS-users)
___
Officially support game resolution 800x520 ([web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + mini-client)

Also partially [support Steam version with resolution 800x480](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F)

Only support English user interface

*It is recommended to play game using mini client

### Contains the following functions:
1. [**ReRun** Dungeons and Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)
2. [Auto fishing](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)
3. Farm  [World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22) / [PVP](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22pvp%22) / [Invasion](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22invasion%22) / [GVG](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22GVG%22) / [Trials](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22trials%22) / [Gauntlet](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22gauntlet%22)
4. [Launch game in mini client using Google Chrome](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))
5. [Do stuffs while AFK](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)

And some extra functions support developers on developing this bot:
- Matrix: read an image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects
- ...

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [JDK 8 (not tested on version 9 and above but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Apache Maven](https://maven.apache.org/install.html)
- [Google Chrome (if you wish to use the mini client)](https://www.google.com/chrome)

### Installation:
1. `git clone https://github.com/9-9-9-9/Bit-Heroes-bot` or download ZIP file then extract
2. Depends on OS:
    - Windows: click and run the file `build.bat`
    - Linux/MacOS: run the following command `./build.sh` on terminal

#### How to use:
### Configuration
Have to configure the `config.properties` (or `user-config.properties`) file first

Watch out the following keys:
- `offset.screen.x`
- `offset.screen.y`

They are the coordinate where your game screen starts. 
How to fill it correctly? [Read me](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Basic-setup)

To enable Telegram notification (require technical skill)
- Set the bot private key to `telegram.token`
- Set the channel id to `telegram.channel-id`

### ReRun
For: everyone

[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)

*This function only supports clicking the ReRun button, that means you have to enter Dungeon/Raid manually, turn on the Auto and when the ReRun button appears, it will be automatically clicked*

- Mini-client or Web:
  - Windows:
    - click and run `rerun.bat`
    - or run from command line: `java -jar BitHeroes.jar rerun <loop_count>`
  - Linux/Mac:
    - run `./rerun.sh` from terminal
    - or command `java -jar BitHeroes.jar rerun <loop_count>`
- Steam:
  - Click and run: `steam.rerun.bat`
  - Run from commandline: `java -jar BitHeroes.jar rerun <loop_count> --steam`

Arguments:
> accept first argument as `loop count` is how many time to click ReRun button before exit

Supported flags:
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./rerun.sh 100 --exit=3600` means will stop ReRun after clicked ReRun buttons 100 times or after 3600 seconds (1 hours), depends which condition completed first
- [Global flags](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Application-flags)

Notes:
- Automatically exit if can not detect the ReRun button within 15 minutes
- Automatically exit after detected the Reconnect button
- Automatically re-active the Auto button if it been red for 60s
- Support click the arrow button when having a conversation
- Push notification to Telegram when detect Reconnect button (critical), not see ReRun within 15m (critical), exit (normal). But only works if you correctly configured Telegram

### Fishing
For: everyone

[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)

*To use this function, you the to be ready on fishing state, and the Start button is visible clearly on the screen*

- Mini-client or Web:
  - Windows:
    - click and run `fishing.bat`
    - or run from command line: `java -jar BitHeroes.jar fishing <hook_count>`
  - Linux/Mac: 
    - run `./fishing.sh` from terminal
    - or command `java -jar BitHeroes.jar fishing <hook_count>`
- Steam:
  - Click and run: `steam.fishing.bat`
  - Run from commandline: `java -jar BitHeroes.jar fishing <hook_count> --steam`
  
Arguments:
> accept first argument as `hook count` is how many times to hook (material consumes) before exit

Supported flags:
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./fishing.sh 20 --exit=1800` means will stop fishing after fishing 20 times or after 1800 seconds (30m), depends which condition completed first
- [Global flags](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Application-flags)

### AFK
For: everyone

[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)

*This function does not support select level/mode, how many badge/ticket/... to consumes and can only everything by default so please chose everything first manually then use this*

- Mini-client or Web:
  - Windows:
    - click and run `afk.bat`
    - or run from command line: `java -jar BitHeroes.jar afk`
  - Linux/Mac: 
    - run `./afk.sh` from terminal
    - or command `java -jar BitHeroes.jar afk`
- Not support Steam

Supported flags:
- `--pvp` do PVP
- `--boss` do world boss
- `--gvg` do GVG
- `--invasion` do Invasion
- `--trials` do Trials
- `--gauntlet` do Gauntlet
- `--all` do everything above
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./afk.sh --exit=1800` means will stop after 1800 seconds (30m)
- [Global flags](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Application-flags)

### Mini client using Google Chrome
For: everyone

[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

Steps:
1. Make sure you can play game on Google Chrome web browser at https://www.kongregate.com/games/Juppiomenz/bit-heroes
2. Press F12 to open Dev Tools
3. Go to Console tab of Dev Tools
4. Open file `prepare-mini-chrome-client.txt`
5. Paste the content into the Console tab
6. Copy the output into `user-config.properties` file, make sure to override the correct properties
7. Generate mini client using:
- Windows: click and run file `client.bat`
- Linux/Mac: run file `./client.sh` from terminal

Enjoy it
- Windows: click and run file `mini-game-on-chrome*.bat`
- Linux/Mac: run file `./mini` or `./mini-game-on-chrome*.sh` from terminal

### Matrix
For: developers only

`./matrix.sh <KeepHexColor> <ColorTolerant> <image> [additional flags]`

Read image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects

[Those output image can be used to scan buttons on screen with minimal fault and work gracefully across OS and devices because it maybe not have to facing with "Gamma correction" issue](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/How-can-this-bot-works-cross-platform%3F)

#### Other features
[find out at our Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

#### Global flags
- `--help` show help for specific application, for example: `./rerun.sh --help`
- `--steam` *(Windows only)* for Bit Heroes on Steam with resolution 800x480
- `--web` *(default, optional)* for Bit Heroes on Web or Mini-client with resolution 800x520
- `--debug` print debug messages, for developers only
- `--img` save screenshot into `./out/images/<app>` directory, only use for debugging purpose
- `--mute` do not push notification to Telegram

#### Tested environments:
Please find out on each function on [wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

### Windows users
If you want to pass parameters/flags to program, you have to edit the `*.bat` files manually

For example if you want to use the feature `--exit=X` on the ReRun function, you have to edit the `rerun.bat` file, modify content
- from `java -jar BitHeroes rerun`
- to `java -jar BitHeroes rerun --exit=1800` if you want to stop after 1800 seconds
- or to `java -jar BitHeroes rerun 30 --exit=1800` if you want to stop after 30 times ReRun or 1800 seconds, depends on what condition completed first

and then save the file

Otherwise you can run app directly from commandline
> java -jar BitHeroes.jar "function_name" "param1" ["param2"] [--flags]

### MacOS users
1. From MacOS 10.13+, java.awt.Robot class of Java can not do mouse and keyboard interaction, thus none of bot functions will work
2. With a bit of luck, you can try to add Java to [System Preferences > Security & Privacy > Accessibility] and see if it works (because MacOS prevent apps from controlling your mac by default).
If it doesn't work, install a higher version of Java, add Accessibility and try again. If it still doesn't work, just give up
3. Thus this whole project only has the mini-client function will work on MacOS

So this project does not officially supported MacOS, any developer can resolve the above issue, please make a pull request 