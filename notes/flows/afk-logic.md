# AFK Script Logic and Flow

The AFK script (`AfkApp.java`) is designed for long-term unattended play. It manages multiple game activities by prioritizing tasks and handling transitions between them.

## The Main Loop

The AFK script runs a continuous loop (`doLoop`) that performs the following checks in order of priority:

1.  **Safety & Maintenance**:
    -   Handles "Are you sure you want to exit?" dialogs.
    -   Handles "Are you still there?" (AFK check) dialogs.
    -   Adjusts screen offset if the window has moved.
2.  **Resource Wait**:
    -   If all selected tasks are "blocked" (waiting for energy/tickets to regenerate), the bot sleeps for a configurable period (default 5 minutes).
3.  **Global Dialogs**:
    -   Checks for "Start with non-full team" or "Confirm quit battle" dialogs.
4.  **Activity Entry**:
    -   Tries to enter specific activities (Raid, Quest, World Boss, Expedition) if they are not currently blocked.
5.  **State-Based Actions**:
    -   Iterates through the `NextAction` lists of all active tasks. If a matching button is found on screen, the bot clicks it.
6.  **Navigation (If Stuck)**:
    -   If no matching images are found for several iterations, the bot attempts to find the "Attendable Place" (the map icon) for the next prioritized task to navigate back into a game activity.

## Task Prioritization

The AFK script generally processes tasks in a fixed order, although this can be influenced by which flags are passed at startup. Common tasks include:
-   Questing
-   Fishing (Claiming bait)
-   PVP
-   World Boss
-   GVG / Invasion / Expedition
-   Trials / Gauntlet
-   Raid

## Task Blocking (The cooldown)

When an activity is completed or the bot runs out of resources (e.g., "Not enough tickets" dialog appears), that task is "blocked" for a specific duration (usually the time it takes to regenerate one ticket or a fixed interval like 30 minutes). This prevents the bot from constantly trying to enter an activity it cannot perform.

## Parallel Monitoring (SmallTasks)

Crucially, while `doLoop` handles the macro-flow, the `SmallTasks` thread handles micro-interactions that can happen at any time:
-   Clicking through NPC dialogue (Talk arrows).
-   Handling familiar persuasion (Captures and Bribes).
-   Reconnecting after a network drop.
-   Closing unexpected news or reward popups.
