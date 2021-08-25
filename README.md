## This is bot for Bit Heroes
##### on Linux / Windows / ~~MacOS~~
[![Github Open Issues](https://img.shields.io/github/issues/9-9-9-9/Bit-Heroes-bot.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/issues)
[![Github All Releases](https://img.shields.io/github/downloads/9-9-9-9/Bit-Heroes-bot/total.svg)](https://github.com/9-9-9-9/Bit-Heroes-bot/releases)

have inspiration from https://github.com/tiemonl/Bit-Heroes-Fishing-Bot

___
[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for developers

Normal users please [read at this page](https://github.com/9-9-9-9/Bit-Heroes-bot/blob/master/README.release.md)

_There're some additional information for MacOS users, please read at bottom of this page_
___
Officially support game resolution 800x520 ([web](https://www.kongregate.com/games/Juppiomenz/bit-heroes) + mini-client)

Also partially [support Steam version with resolution 800x480](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Does-this-bot-supports-Steam-version-of-Bit-Heroes%3F)

*It is recommended to play game using mini client

### Contains the following functions:
1. Detect and click **ReRun** button while in Dungeons and Raid
2. Auto fishing
3. Launch game in mini client using Google Chrome

And some extra functions support developers on developing this bot:
- Matrix: read an image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects
- ...

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- [JDK 8 (not tested on version 9 and above but probably it works)](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Apache Maven](https://maven.apache.org/install.html)
- [Google Chrome (if you wish to use the mini client)](https://www.google.com/chrome)

### Installation:
1. `git clone https://github.com/9-9-9-9/Bit-Heroes-bot` or download ZIP file extract
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

*This function only supports clicking the ReRun button, that means you have to enter Dungeon/Raid manually, turn on the Auto and when the ReRun button appears, it will be automatically clicked*

- Mini-client:
  - Windows: click and run `rerun.bat`
  - Linux/Mac: run `./rerun.sh` from terminal
- Steam:
  - Click and run: `steam.rerun.bat`
  - Run from commandline: `java -jar BitHeroes.jar rerun <loop_count> --steam`

Arguments:
> accept first argument as `loop count` is how many time to click ReRun button before exit

Supported flags:
> `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./rerun.sh 100 --exit=3600` means will stop ReRun after clicked ReRun buttons 100 times or after 3600 seconds (1 hours), depends which condition completed first

Notes:
- Automatically exit if can not detect the ReRun button within 15 minutes
- Automatically exit after detected the Reconnect button 
- Support click the arrow button when having a conversation
- Push notification to Telegram when detect Reconnect button (critical), not see ReRun within 15m (critical), exit (normal). But only works if you correctly configured Telegram

### Fishing
For: everyone

*To use this function, you the to be ready on fishing state, and the Start button is visible clearly on the screen*

- Mini-client:
  - Windows: click and run `fishing.bat`
  - Linux/Mac: run `./fishing.sh` from terminal
- Steam:
  - Click and run: `steam.fishing.bat`
  - Run from commandline: `java -jar BitHeroes.jar fishing <hook_count> --steam`
  
Arguments:
> accept first argument as `hook count` is how many times to hook (material consumes) before exit

Supported flags:
> `--exit=X`

### Mini client using Google Chrome
For: everyone

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
- Windows: click and run file `mini-game-on-chrome.bat`
- Linux/Mac: run file `./mini` or `./mini-game-on-chrome.sh` from terminal

### Matrix
For: developers only

`./matrix.sh <KeepHexColor> <ColorTolerant> <image> [additional flags]`

Read image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects

[Those output image can be used to scan buttons on screen with minimal fault and work gracefully across OS and devices because it maybe not have to facing with "Gamma correction" issue](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/How-can-this-bot-works-cross-platform%3F)

#### Project-wide flags
- `--help` show help for specific application, for example: `./rerun.sh --help`
- `--debug` print debug messages, for developers only
- `--img` save screenshot into `./out/images/<app>` directory, only use for debugging purpose
- `--mute` do not push notification to Telegram

#### Tested environments:
| OS | Result |
| --- | --- |
| Ubuntu 18.04 (development environment) | Work perfectly |
| Windows 10 x64 | Work perfectly |
| Windows 7 x64 (Virtual Box) | Work perfectly |
| Steam on Windows 7 x64 (Virtual Box) | Work good (basically, not stable) |
| MacOS 10.14 High Sierra | Bot functions not work, only mini-client works |

### Windows users
If you want to pass parameters/flags to program, you have to edit the `*.bat` files manually

For example if you want to use the feature `--exit=X` on the ReRun function, you have to edit the `rerun.bat` file, modify content
- from `java -jar BitHeroes rerun`
- to `java -jar BitHeroes rerun --exit=1800` if you want to stop after 1800 seconds
- or to `java -jar BitHeroes rerun 30 --exit=1800` if you want to stop after 30 times ReRun or 1800 seconds, depends on what condition completed first

and then save the file

Otherwise you can run app directly from commandline
> java -jar BitHeroes.jar <function_name> <param1> [<param2> [--flags]]

### MacOS users
1. From MacOS 10.13+, java.awt.Robot class of Java can not do mouse and keyboard interaction, thus none of bot functions will work
2. With a bit of luck, you can try to add Java to [System Preferences > Security & Privacy > Accessibility] and see if it works (because MacOS prevent apps from controlling your mac by default).
If it doesn't work, install a higher version of Java, add Accessibility and try again. If it still doesn't work, just give up
3. Thus this whole project only has the mini-client function will work on MacOS

So this project does not officially supported MacOS, any developer can resolve the above issue, please make a pull request 