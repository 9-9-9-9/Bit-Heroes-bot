# Bit Heroes Bot - Technical Architecture Overview

The Bit Heroes Bot is a Java-based automation tool designed to play Bit Heroes. It operates by capturing screen content, analyzing it using image comparison techniques, and simulating user input (mouse clicks and keypresses) to interact with the game.

## Key Technologies

- **Java**: The core language used for the bot's logic and execution.
- **Java AWT Robot**: Used for capturing screenshots and simulating low-level mouse and keyboard events.
- **Image Comparison**: A custom implementation that uses "Black/Non-Black Pixel" matrices for robust pattern matching, allowing it to handle minor visual variations in the game.
- **Multi-threading**: The bot uses multiple threads to handle core loops, UI monitoring (SmallTasks), and screen offset detection simultaneously.

## Core Components

### 1. Application Layer (`bh.bot.app`)
Contains the high-level logic for different game modes:
- `AfkApp`: The main entry point for non-stop automated play.
- `Farming Apps` (e.g., `PvpApp`, `RaidApp`): Specialized applications for specific game activities, often extending `AbstractDoFarmingApp`.
- `GenMiniClient`: Handles the generation of a specialized Chrome-based mini-client for optimal performance.

### 2. Utility Layer (`bh.bot.common.utils`)
Provides essential services:
- `ImageUtil`: Handles image loading, color similarity checks, and transformations.
- `InteractionUtil`: Wraps Java `Robot` for mouse and keyboard simulation and screen capturing.
- `ThreadUtil`: Manages timing and synchronization.

### 3. Type System (`bh.bot.common.types`)
Defines data structures used across the bot:
- `BwMatrixMeta`: Represents a target image/pattern to find on the screen.
- `Offset`: Represents coordinates relative to the game window.
- `UserConfig`: Stores user-specific settings and profiles.

## Execution Model

The bot generally follows a state-machine-like loop:
1. **Screen Capture**: Take a screenshot of the relevant game area.
2. **State Identification**: Compare the screenshot against a set of predefined `BwMatrixMeta` objects (buttons, labels, dialogs).
3. **Action Execution**: If a match is found, execute the associated action (e.g., click the button, press a key).
4. **Transition**: Wait for a short interval and repeat.

## SmallTasks (The Watcher)

While the main application loop runs, a set of "SmallTasks" operates in parallel to handle global events:
- Closing "Are you still there?" dialogs.
- Reconnecting if disconnected.
- Handling "Persuade" dialogs for familiars.
- Claiming daily rewards.
- Closing news popups.
