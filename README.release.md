## Bit Heroes bot
This readme file is for normal users

Developers please read [at my github](https://github.com/9-9-9-9/Bit-Heroes-bot)
___
Only support game resolution 800x520

Therefore:
- Can only use this bot to control game on web version at [kongregate.com](https://www.kongregate.com/games/Juppiomenz/bit-heroes)
- __Can not__ use for Steam version which does not have Window size 800x520 option
- __Not sure__ about Android emulators (like Nox, Blue Stacks,..) because not tested

*It is recommended to play game using mini client (read bellow)*

### Contains the following functions:
1. Fishing: auto fishing
2. Re-Run: detect and click ReRun button while in Dungeons and Raid
3. Launch game in mini client using Google Chrome

Also supports pushing messages to Telegram for notification purpose

### Minimum requirement:
- Java 8
- Google Chrome installed (if you wish to use the mini client)

### Configure:
Have to configure the user-config.properties file

Watch out the following keys:
- `offset.screen.x`
- `offset.screen.y`

They are the coordinate where your game screen starts. How to fill it correctly?
1. Open game at https://www.kongregate.com/games/Juppiomenz/bit-heroes on Google Chrome web browser, move the window to the top left of your screen
2. Press the `Print Screen` button
3. Paste it into Paint or something similar
4. Point at the top left of your GAME SCREEN to see its current coordinate
5. Fill the number into `user-config.properties` file. For example if current position is `0,57`, fill it like this:
    - `offset.screen.x=0`
    - `offset.screen.y=57`
    - On Windows, usually x=8 and y=31 when using mini client of Google Chrome

To enable Telegram notification (require technical skill)
- Set the bot private key to `telegram.token`
- Set the channel id to `telegram.channel-id`

#### How to use:
### ReRun
- Windows: run file `.\rerun.bat`
- Other OS: run file `./rerun.sh`

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
- Other OS: run file `./fishing.sh`

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
- Other OS: run file `./client.sh`

Enjoy it
- Windows: run file `.\mini-game-on-chrome.bat`
- Other OS: run file `./mini` or `./mini-game-on-chrome.sh`

#### Project-wide flags
- `--help` show help for specific application, for example: `./rerun.sh --help`
- `--mute` do not push notification to Telegram
- `--img` save screenshot into `./out/images/<app>` directory, only use for debugging purpose