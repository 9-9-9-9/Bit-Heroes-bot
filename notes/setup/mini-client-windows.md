# Setting up the Mini-Client on Windows

The Mini-Client is a specialized Google Chrome instance that runs Bit Heroes in a dedicated window with a fixed resolution. This is the recommended way to run the bot on Windows for maximum reliability.

## Prerequisites

1.  **Google Chrome**: Must be installed on your Windows system.
2.  **Java 8**: Ensure Java 8 is installed and available in your PATH.
3.  **Display Scaling**: Your Windows screen scale ratio must be set to **100%** (no scaling).

## Step-by-Step Installation

### 1. Extract Web Credentials
To allow the mini-client to log into your account, you need to extract your Kongregate credentials:
1.  Open Chrome and go to [Bit Heroes on Kongregate](https://www.kongregate.com/games/Juppiomenz/bit-heroes).
2.  Press `F12` to open Developer Tools.
3.  Go to the **Console** tab.
4.  Open the file `prepare-mini-chrome-client.txt` in the bot's root directory.
5.  Copy the entire content of that file and paste it into the Chrome Console, then press `Enter`.
6.  The console will output several lines of configuration (e.g., `1.game.kong.user.id=...`).

### 2. Configure `user-config.properties`
1.  Open `user-config.properties` in a text editor.
2.  Paste the lines you copied from the Chrome Console into this file.
3.  Ensure `external.application.chrome.path` points to your `chrome.exe`. Use double backslashes (e.g., `C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe`).

### 3. Generate the Mini-Client
1.  Run `client.bat` from the bot's root directory.
2.  This will create a `bh-client/` directory and a `mini-game-on-chrome1.bat` file.
3.  The console should confirm "Generated file 'mini-game-on-chrome1.bat'".

### 4. Launch the Game
1.  Double-click `mini-game-on-chrome1.bat` to launch the dedicated game window.
2.  Ensure the window appears and logs into your account correctly.

### 5. Start the Bot
1.  Once the game is running, you can start the bot using `web.bot.bat`.
2.  Select the desired function (e.g., `afk`) and your profile.

## Troubleshooting

-   **Chrome Not Found**: Double-check the path in `user-config.properties`.
-   **Wrong Resolution**: Ensure your Windows scaling is 100%. The mini-client window should be approximately 820x565 (to accommodate the 800x520 game area plus borders).
-   **Login Fails**: Re-run the console script and ensure you've copied the `auth_token` correctly. Auth tokens can expire, so you may need to repeat Step 1 if you haven't used the bot for a long time.
