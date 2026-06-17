# Interaction and Simulation

The bot interacts with the game by simulating low-level hardware events using the Java `AWT Robot` class.

## InteractionUtil

`InteractionUtil` is the primary interface for all simulations. It is divided into `Keyboard`, `Mouse`, and `Screen` sub-classes.

### Mouse Interactions
- **Movement**: `robot.mouseMove(x, y)` moves the cursor.
- **Clicking**: `robot.mousePress` and `robot.mouseRelease` are used with `InputEvent.BUTTON1_DOWN_MASK`.
- **Hiding the Cursor**: After many actions, the bot moves the mouse to `(0, 0)` (relative to the game window) to prevent it from hovering over UI elements and triggering tooltips that might interfere with image recognition.

### Keyboard Interactions
The bot uses keypresses for fast navigation and clearing dialogs:
- `SPACE`: Often used to clear dialogs or confirm actions.
- `ENTER`: Used for confirmation.
- `ESC`: Crucial for closing windows, backing out of menus, or canceling actions.

## Timing and Delays

Simulating interaction requires careful timing. The bot uses `ThreadUtil.sleep()` to ensure the game has time to react to an input before the next action is attempted.

Common delays:
- 300ms after moving the mouse before clicking.
- 50ms between mouse press and release.
- 5s (default) between main loop iterations.

## Reliability Features

- **Spamming ESC**: In some error states or transitions, the bot will "spam" the ESC key to ensure all windows are closed and it's back at a known state.
- **Check Game Screen Offset**: The bot periodically re-verifies the position of the game window to handle accidental movement.
