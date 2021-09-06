## This is bot for Bit Heroes, called 99bot
##### on Linux / Windows / ~~MacOS~~
[![Github Open Issues](https://img.shields.io/github/issues/9-9-9-9/Bit-Heroes-bot.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/issues)
[![Github All Releases](https://img.shields.io/github/downloads/9-9-9-9/Bit-Heroes-bot/total.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/releases)

have inspiration from https://github.com/tiemonl/Bit-Heroes-Fishing-Bot

> Hi, I'm 99bot, please give this repo a Star, thanks
___
[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for developers

Normal users please [read at this page](https://github.com/9-9-9-9/Bit-Heroes-bot/blob/master/README.release.md)

[_There're some notes for MacOS users, please read here_](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Notes-for-MacOS-users)
___
Officially support game resolution 800x520 ([web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + mini-client) and [Steam version with resolution 800x480](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F)

**Only support English user interface**

### Contains the following functions:
1. [**ReRun** Dungeons and Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)
2. [Auto fishing](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)
3. [Do stuffs while AFK](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)
4. Farm  [World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22) / [Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22raid%22) / [PVP](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22pvp%22) / [Invasion](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22invasion%22) / [GVG](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22GVG%22) / [Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22expedition%22) / [Trials](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22trials%22) / [Gauntlet](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22gauntlet%22)
5. [Launch game in mini client using Google Chrome](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

And some extra functions support developers on developing this bot

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [JDK 8 (not tested on version 9 and above but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Apache Maven](https://maven.apache.org/install.html)
- [Google Chrome (if you wish to use the mini client)](https://www.google.com/chrome)

### Installation:
1. `git clone https://github.com/9-9-9-9/Bit-Heroes-bot` or download ZIP file then extract
2. Depends on OS:
    - Windows: click and run the file `build.bat`
    - Linux/MacOS: run command `./build.sh` in terminal

#### Easy to use:
1. Configure follow [instruction on Wiki (click me)](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Basic-setup)
2. Launch bot:
  - Steam on Windows: `steam.bot.bat`
  - Mini-client/Web on Windows: `bot.bat`
  - Linux/MacOS: `./bot.sh`
  
Tips 1: you can launch app with flags directly via command-line after got familiar with 99bot:
> java -jar BitHeroes.jar "function_name" "param1" ["param2"] [--flags]

Tips 2: see help
- Windows: run file `help.bat`
- Linux/MacOS: run script `./help.sh`

##### Optional configuration:
- [Configure Raid/World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Select-Raid-World-Boss-level,-mode,..-using-%60setting%60-function):
  - Windows: run file `setting.bat`
  - Linux/MacOS: run script `./setting.sh`
- [push notification via Telegram](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

#### Tested environments:
Please find out on each function on [wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

### MacOS users
1. From MacOS 10.13+, java.awt.Robot class of Java can not do mouse and keyboard interaction, thus none of bot functions will work
2. With a bit of luck, you can try to add Java to [System Preferences > Security & Privacy > Accessibility] and see if it works (because MacOS prevent apps from controlling your mac by default).
If it doesn't work, install a higher version of Java, add Accessibility and try again. If it still doesn't work, just give up
3. Thus this whole project only has the mini-client function will work on MacOS

So this project does not officially supported MacOS, any developer can resolve the above issue, please make a pull request 