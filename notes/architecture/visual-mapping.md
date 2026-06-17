# Visual State Mapping and Image Comparison

The bot's ability to "see" the game is built on a custom image comparison system that translates visual screen data into actionable game states.

## BwMatrixMeta: The Core of Vision

Unlike simple pixel-by-pixel matching, the bot uses `BwMatrixMeta` to represent game elements (buttons, labels, dialogs).

### Black and Non-Black Pixels
A `BwMatrixMeta` object doesn't store the entire image. Instead, it stores:
- **Black Pixels**: A set of coordinates that *must* match a specific "target color" (or be within a similarity threshold).
- **Non-Black Pixels**: A set of coordinates that *must NOT* match the target color.

This approach is highly effective because:
- It handles transparency and varying backgrounds.
- It focuses on the unique features of a UI element.
- It is computationally faster than comparing full images.

### Color Similarity and Tolerance
The `ImageUtil.areColorsSimilar` method determines if two pixels match. It uses a `tolerance` value (configurable in `user-config.properties`) to allow for slight color variations caused by rendering differences or transparency.

```java
public static boolean areColorsSimilar(int rgb1, int rgb2, int tolerance) {
    return isMatch(getRed(rgb1), getRed(rgb2), tolerance)
            && isMatch(getGreen(rgb1), getGreen(rgb2), tolerance)
            && isMatch(getBlue(rgb1), getBlue(rgb2), tolerance);
}
```

## Mapping Visuals to Actions

The bot uses `NextAction` objects to link a visual state (`BwMatrixMeta`) to a logical action.

```java
public static class NextAction {
    public final BwMatrixMeta image;           // The visual pattern to look for
    public final boolean reduceLoopCountOnFound; // Should this decrement the remaining loops?
    public final boolean isOutOfTurns;           // Does finding this mean we are finished with this activity?
}
```

### Searching for Elements
The `InteractionUtil.Screen.Game` class provides methods to find these patterns on the screen:
- `captureElementInEstimatedArea`: Captures a specific region where a button is expected to be.
- `findByScanColumn` / `findByScanScreen`: Scans larger areas to find elements that might move (like "Attendable Places" on the main map).

## Screen Resolution and Offsets

The bot is designed to work with an **800x520** internal resolution. It uses a "Resolution Profile" to define where UI elements are expected to appear.

### Game Screen Offset
To work across different window positions, the bot detects the "Game Screen Offset" (the top-left corner of the actual game area). All coordinates are then calculated as:
`Screen Coordinate = Game Offset + Relative Element Coordinate`
