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
Only support game resolution 800x520

Therefore:
- Can only use this bot to control game on web version at [kongregate.com](https://www.kongregate.com/games/Juppiomenz/bit-heroes)
- __Can not__ use for Steam version which does not have Window size 800x520 option
- __Not sure__ about Android emulators (like Nox, Blue Stacks,..) because not tested

### Contains the following functions:
1. Detect and click **ReRun** button while in Dungeons and Raid
2. Auto fishing
3. Launch game in mini client using Google Chrome

And some extra functions support developers on developing this bot:
- SamePix: read from 2 images (must be same size) and yield a new picture with only pixels equally from original pictures
- Matrix: read an image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects
- KeepPix: read 2 images are source and input, filter pixels from the input image, only keep pixels which exists in source image

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- JDK 8 (not tested on version 9 and above but probably it works)
- Maven
- Google Chrome (if you wish to use the mini client)

### Installation:
1. `git clone https://github.com/9-9-9-9/Bit-Heroes-bot` or download ZIP file extract
2. Depends on OS:
    - Windows: click and run the file `.\build.bat`
    - Linux: run the following command `./build.sh` on terminal
    - MacOS: run the following command `bash build.sh` on terminal

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

`./rerun.sh <how many times to click rerun>`

or click and run `.\rerun.bat` on Windows

for example: `./rerun.sh 10` means will try to click ReRun 10 times before exit

Supported flags:

`--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./rerun.sh 100 --exit=3600` means will stop ReRun after clicked ReRun buttons 100 times or after 3600 seconds (1 hours), depends which condition completed first

Notes:
- Automatically exit if can not detect the ReRun button within 15 minutes
- Automatically exit after detected the Reconnect button 
- Support click the arrow button when having a conversation
- Push notification to Telegram when detect Reconnect button (critical), not see ReRun within 15m (critical), exit (normal). But only works if you correctly configured Telegram

### Fishing
For: everyone

`./fishing.sh <how many times to hook>`

or click and run `.\fishing.bat` on Windows

for example: `./fishing.sh 10` means will try to hook 10 times before exit

Supported flags:

`--exit=X`

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
- Windows: run file `.\client.bat`
- Linux/Mac: run file `./client.sh`

Enjoy it
- Windows: run file `.\mini-game-on-chrome.bat`
- Linux/Mac: run file `./mini` or `./mini-game-on-chrome.sh`

### SamePix
For: developers only

`./samepix.sh <image1> <image2> [additional flags]`

This command will load 2 images from argument, scan them, find the same pixels from both images and save it to another image localed at `./out/same-pix-images` directory

Those output image can be used to scan buttons on screen with minimal fault and work gracefully across OS and devices because it maybe not have to facing with "Gamma correction" issue

Check the `rerun.bmp`, `rerun-hl.bmp` and `rerun-sp.bmp` in resources folder, you will see:
- `rerun.bmp` is the ReRun in game button when was not highlighted
- `rerun-hl.bmp` is when it was highlighted
- `rerun-sp.bmp` is the output of SamePix where only same pixel from those 2 pictures are kept and it was used to detect ReRun button in game.

### Matrix
For: developers only

`./matrix.sh <KeepHexColor> <image> [additional flags]`

Read image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects

Those output pictures can be used to scan buttons on screen with minimal fault and work gracefully across OS and devices because it maybe not have to facing with "Gamma correction" issue

### KeepPix
For: developers only

`./keepix.sh <source> <input> [additional flags]`

Read 2 images: source and input, filter pixels from the input image, only keep pixels which exists in source image

Those output pictures can be used to scan buttons on screen with minimal fault and work gracefully across OS and devices because it maybe not have to facing with "Gamma correction" issue

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
| MacOS 10.14 High Sierra | Bot functions not work, only mini-client works |

### MacOS users
1. From MacOS 10.13+, java.awt.Robot class of Java can not do mouse and keyboard interaction, thus none of bot functions will work
2. With a bit of luck, you can try to add Java to [System Preferences > Security & Privacy > Accessibility] and see if it works (because MacOS prevent apps from controlling your mac by default).
If it doesn't work, install a higher version of Java, add Accessibility and try again. If it still doesn't work, just give up
3. Thus this whole project only has the mini-client function will work on MacOS

So this project does not officially supported MacOS, any developer can resolve the above issue, please make a pull request 