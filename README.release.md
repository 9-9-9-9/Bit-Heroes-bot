Upgrade notes:
- Move the follow files and directory from previous bot version's directory
  - `chrome-user-dir` directory, that folder was created by chrome to do temp cache and also save your game's setting
  - `user-config.properties` file, it contains your configurations
___
## Bit Heroes bot
##### on Linux / Windows / ~~MacOS~~

**If you like my bot, please give this repo a Star, thanks**

[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for normal users

Developers please read [at my github](https://github.com/9-9-9-9/Bit-Heroes-bot)

[_There're some notes for MacOS users, please read here_](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Notes-for-MacOS-users)
___
Officially support game resolution 800x520 ([web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + mini-client)

Also [partially support Steam version with resolution 800x480](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F)

Only support English user interface

*It is recommended to play game using mini client

### Contains the following functions:
1. [**ReRun** Dungeons and Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)
2. [Auto fishing](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)
3. Farm  [World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22) / [PVP](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22pvp%22) / [Invasion](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22invasion%22) / [GVG](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22GVG%22) / [Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22expedition%22) / [Trials](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22trials%22) / [Gauntlet](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22gauntlet%22)
4. [Launch game in mini client using Google Chrome](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))
5. [Do stuffs while AFK](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [Java 8 (not tested on version 9+ but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Google Chrome installed (if you wish to use the mini client)](https://www.google.com/chrome)

### Configure:
Have to configure the user-config.properties file

Watch out the following keys:
- `offset.screen.x`
- `offset.screen.y`

They are the coordinate where your game screen starts. 
How to fill it correctly? [Read me](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Basic-setup)

To enable Telegram notification, [follow me](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)
- Set the bot private key to `telegram.token`
- Set the channel id to `telegram.channel-id`

#### How to use:
### ReRun
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

Supported flags:
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./fishing.sh 20 --exit=1800` means will stop fishing after fishing 20 times or after 1800 seconds (30m), depends which condition completed first
- [Global flags](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Application-flags)

### Mini client using Google Chrome
[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

Steps:
1. Make sure you can play game on Google Chrome web browser at https://www.kongregate.com/games/Juppiomenz/bit-heroes
2. Press F12 to open Dev Tools
3. Go to Console tab of Dev Tools
4. Open file `prepare-mini-chrome-client.txt`
5. Paste the content into the Console tab
6. Copy the output into `user-config.properties` file, make sure to override the correct properties
7. Generate mini client using:
- Windows: run file `client.bat`
- Linux/Mac: run file `./client.sh` from terminal

Enjoy it
- Windows: click and run file `mini-game-on-chrome*.bat`
- Linux/Mac: run file `./mini` or `./mini-game-on-chrome*.sh` from terminal

### AFK
[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)

*This function does not support select level/mode, how many badge/ticket/... to consumes and can only everything by default so please chose everything first manually then use this*

- Mini-client or Web:
  - Windows:
    - click and run `afk.bat`
    - or run from command line: `java -jar BitHeroes.jar afk`
  - Linux/Mac: 
    - run `./afk.sh` from terminal
    - or command `java -jar BitHeroes.jar afk`
- Steam **(not yet supported Invasion & Trials):**
  - Click and run: `steam.afk.bat`
  - Run from commandline: `java -jar BitHeroes.jar afk --steam`

Supported flags:
- `--pvp` do PVP
- `--boss` do world boss [(require setting)](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Select-Raid-World-Boss-level,-mode,..-using-%60setting%60-function)
- `--gvg` do GVG
- `--invasion` do Invasion
- `--expedition` do Expedition
- `--trials` do Trials
- `--gauntlet` do Gauntlet
- `--raid` do Raid [(require setting)](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Select-Raid-World-Boss-level,-mode,..-using-%60setting%60-function)
- `--all` do everything above
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./afk.sh --exit=1800` means will stop after 1800 seconds (30m)
- `--profile=X` specific configuration profile (contains which Raid/World Boss to farm, which Raid mode Normal/Hard/Heroic to select)
- [Global flags](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Application-flags)

#### Other features
[find out at our Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

#### Global flags
- `--help` show help for specific application, for example: `./rerun.sh --help`
- `--steam` *(Windows only)* for Bit Heroes on Steam with resolution 800x480
- `--web` *(default, optional)* for Bit Heroes on Web or Mini-client with resolution 800x520
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