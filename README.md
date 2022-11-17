## Sadly, most functions bot no longer works with new update Nov 16th, 2022
### Fixed functions:
- [x] Fishing (2.11.0)
- [x] ReRun (2.11.0)
- [x] Raid (2.12.0)
- [ ] PVP
- [ ] WB Solo
- [ ] WB Team
- [ ] GVG
- [ ] Invasion
- [ ] Idol Dimension (Expedition)
- [ ] Inferno Dimension (Expedition)
- [ ] Hallowed Dimension (Expedition)
- [ ] Jammie Dimension (Expedition)
- [ ] Battle Bards (Expedition)
- [ ] Trials
- [ ] Gauntlet
- [ ] AFK

___
**Upgrade notes:** copy configuration files `user-config.properties` and `readonly.*.user-config.properties` from previous bot version's directory _(if you update the bot via auto-update method then no need to do this)_
___
## Bit Heroes bot
##### on Linux / Windows

[![Github Open Issues](https://img.shields.io/github/issues/9-9-9-9/Bit-Heroes-bot.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/issues)
[![Github All Releases](https://img.shields.io/github/downloads/9-9-9-9/Bit-Heroes-bot/total.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/releases)

> We play the game, don't let the game plays us

[Wiki](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

> To the guys who using this bot but not giving this repo a Star: Fvck U
___
Supports **English user interface only** of [Bit Heroes on Steam](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F) and [Bit Heroes on web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + [mini-client](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

**Beware:** Make sure your screen scale ratio is 100% (original, no scale) [(check this article)](https://www.windowscentral.com/how-set-custom-display-scaling-setting-windows-10)

### Contains the following functions:
1. [Auto fishing](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22fishing%22)
2. [**ReRun** Dungeons and Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22rerun%22)
3. [Do stuffs while AFK](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22)
4. Farm  [World Boss](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22) / [Raid](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22raid%22) / [PVP](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22pvp%22) / [Invasion](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22invasion%22) / [GVG](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22GVG%22) / [Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22expedition%22) / [Trials](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22trials%22) / [Gauntlet](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22gauntlet%22)
5. Farm [World Boss in Team mode](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22world-boss%22-(team))
6. [Launch game in mini client using Google Chrome](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22client%22-(mini-client-on-Chrome))

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [Java 8 (not tested on version 9+ but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Google Chrome installed (if you wish to use the mini client)](https://www.google.com/chrome)

#### Easy to use:
1. Configure follow [instruction on Wiki (click me)](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Basic-setup)
2. Launch bot:
  - Steam on Windows: `steam.bot.bat`
  - Mini-client/Web on Windows: `web.bot.bat`
  - Linux: `./bot.sh`
  
Tips 1: you can launch app with flags directly via command-line after got familiar with this bot:
> java -jar BitHeroes.jar "function_name" "param1" ["param2"] [--flags]

Tips 2: see help
- Windows: run file `help.bat`
- Linux: run script `./help.sh`

##### Optional configuration:
- [Configure Raid/World Boss/Expedition](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Select-Raid-World-Boss-level,-mode,..-using-%60setting%60-function):
  - Windows: run file `setting.bat`
  - Linux: run script `./setting.sh`
- [push notification via Telegram](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

##### Some convenient scrips:
_(Example for Windows only, you can save into a bat file to be able to click-to-run)_
- AFK (do chaining tasks PVP, WB, GVG/Invasion/Expedition, TG, Raid) and exit
    > call steam.bot.bat afk a --ear
    
    You can also provide flag `--profile=X` so you won't need to select profile manually
    > call steam.bot.bat afk a --ear --profile=MyProfileName
- AFK and keep online (does not recover from disconnected)
    > call steam.bot.bat afk a
- Fishing
    > call steam.bot.bat fishing
- ReRun
    > call steam.bot.bat rerun
- Explore the `./sample-scripts/` folder

[Learn more about how to create script files here](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Script-example)