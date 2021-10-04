Upgrade notes:
- Move the following file and directory from previous bot version's directory
  - `user-config.properties` and `readonly.*.user-config.properties` files, it contains your configurations
  - `chrome-user-dir` directory, that folder was created by chrome to do temp cache and also save your game's setting. By specify an external directory into the key `external.mini-client.user.dir` of the `user-config.properties` file, next time when you upgrade this bot, you don't need to move this folder
___
## [bh99bot.com](https://bh99bot.com) is the bot for Bit Heroes
##### on Linux / Windows / ~~MacOS~~

> Hi, I'm 99bot, please give this repo a Star, thanks

[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for normal users

Developers please read [at my github](https://github.com/9-9-9-9/Bit-Heroes-bot)

[_There're some notes for MacOS users, please read here_](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Notes-for-MacOS-users)
___
Officially support [BitHeroes on Steam](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F) and ([BitHeroes on web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + mini-client)

**Only support English user interface**

### Contains the following functions:
1. [Auto fishing](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)
2. [**ReRun** Dungeons and Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)
3. [Do stuffs while AFK](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)
4. Farm  [World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22) / [Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22raid%22) / [PVP](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22pvp%22) / [Invasion](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22invasion%22) / [GVG](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22GVG%22) / [Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22expedition%22) / [Trials](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22trials%22) / [Gauntlet](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22gauntlet%22)
5. [Launch game in mini client using Google Chrome](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [Java 8 (not tested on version 9+ but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Google Chrome installed (if you wish to use the mini client)](https://www.google.com/chrome)

#### Easy to use:
1. Configure follow [instruction on Wiki (click me)](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Basic-setup)
2. Launch bot:
  - Steam on Windows: `steam.bot.bat`
  - Mini-client/Web on Windows: `web.bot.bat`
  - Linux/MacOS: `./bot.sh`
  
Tips 1: you can launch app with flags directly via command-line after got familiar with 99bot:
> java -jar BitHeroes.jar "function_name" "param1" ["param2"] [--flags]

Tips 2: see help
- Windows: run file `help.bat`
- Linux/MacOS: run script `./help.sh`

##### Optional configuration:
- [Configure Raid/World Boss/Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Select-Raid-World-Boss-level,-mode,..-using-%60setting%60-function):
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