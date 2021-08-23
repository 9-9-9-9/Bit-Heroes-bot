## Bit Heroes bot
##### on Linux / Windows / ~~MacOS~~

[Wiki version](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki)

This readme file is for normal users

Developers please read [at my github](https://github.com/9-9-9-9/Bit-Heroes-bot)

_There're some additional information for MacOS users, please read at bottom of this page_
___
Only support game resolution 800x520

Therefore:
- Can only use this bot to control game on web version at [kongregate.com](https://www.kongregate.com/games/Juppiomenz/bit-heroes)
- __Can not__ use for Steam version which does not have Window size 800x520 option
- __Not sure__ about Android emulators (like Nox, Blue Stacks,..) because not tested

*It is recommended to play game using mini client

### Contains the following functions:
1. Detect and click **ReRun** button while in Dungeons and Raid
2. Auto fishing
3. Launch game in mini client using Google Chrome

[Also supports pushing messages to Telegram for notification purpose](https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Configure-Telegram-in-able-to-receive-notification)

### Minimum requirement:
- Java 8 (not tested on version 9 and above but probably it works)
- Google Chrome installed (if you wish to use the mini client)

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
- Windows: run file `.\rerun.bat`
- Linux/Mac: run file `./rerun.sh`

Supported flags:
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./rerun.sh 100 --exit=3600` means will stop ReRun after clicked ReRun buttons 100 times or after 3600 seconds (1 hours), depends which condition completed first
- and global flags (read at the end of this page)

Notes:
- Automatically exit if can not detect the ReRun button within 15 minutes
- Automatically exit after detected the Reconnect button 
- Support click the arrow button when having a conversation
- Push notification to Telegram when detect Reconnect button (critical), not see ReRun within 15m (critical), exit (normal). But only works if you correctly configured Telegram

### Fishing
- Windows: run file `.\fishing.bat`
- Linux/Mac: run file `./fishing.sh`

Supported flags:
- `--exit=X` means will exit after X secs if not completed, no matter how many loop remaining. Usage: `./fishing.sh 20 --exit=1800` means will stop fishing after fishing 20 times or after 1800 seconds (30m), depends which condition completed first
- and global flags (read at the end of this page)

### Mini client using Google Chrome
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

#### Project-wide flags
- `--help` show help for specific application, for example: `./rerun.sh --help`
- `--mute` do not push notification to Telegram
- `--img` save screenshot into `./out/images/<app>` directory, only use for debugging purpose

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