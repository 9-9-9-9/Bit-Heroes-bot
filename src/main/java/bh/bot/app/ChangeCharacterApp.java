package bh.bot.app;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

@AppMeta(code = "change-character", name = "Change Character", displayOrder = 1, argType = "number", argAsk = "What character slot do you want to pick?", argDefault = "1", argRequired = true)
@RequireSingleInstance
public class ChangeCharacterApp extends AbstractApplication {

    private static final Offset[] characterSelectionButtonOffsets = new Offset[]{
            new Offset(250, 200), // # 1
            new Offset(375, 200), // # 2
            new Offset(500, 200), // # 3
    };

    @Override
    protected void internalRun(String[] args) {
        int arg;
        try {
            arg = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            info(getHelp());
            arg = readInputLoopCount("What character slot do you want to pick?");
        }

        final int characterSlot = arg;

        if (characterSlot < 1 || characterSlot > characterSelectionButtonOffsets.length) {
            err("Invalid character slot, must be in range 1 to %d", characterSelectionButtonOffsets.length);
            System.exit(1);
        }

        info("Character in slot %d", characterSlot);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoopClickImage(characterSlot, masterSwitch),
                () -> internalDoSmallTasks( //
                        masterSwitch, //
                        SmallTasks //
                                .builder() //
                                .clickTalk() //
                                .clickDisconnect() //
                                .autoExit() //
                                .detectChatboxDirectMessage() //
                                .build() //
                ), //
                () -> doCheckGameScreenOffset(masterSwitch));
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoopClickImage(int characterSlot, AtomicBoolean masterSwitch) {
        info(ColorizeUtil.formatInfo, "\n\nStarting Character Change");
        Main.warningSupport();
        try {
            final int mainLoopInterval = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());

            moveCursor(new Offset(100, 500).toScreenCoordinate());
            boolean characterLoaded = false;
            boolean loadedSelect = false;
            boolean characterSelected = false;
            int loopCount = 0;
            while (!characterLoaded && !masterSwitch.get()) {
                sleep(mainLoopInterval);
                if (clickImage(BwMatrixMeta.Metas.Character.Buttons.characterSelect)) {
                    debug("Loading Character Selection");
                }
                if (characterSelected) {
                    loopCount += 1;
                    if (loopCount > 10) {
                        debug("We probably loaded the character, or already on it, so breaking out of the loop!");
                        InteractionUtil.Keyboard.sendEscape();
                        InteractionUtil.Keyboard.sendEscape();
                        break;
                    }
                }
                if (loadedSelect) {
                    if (characterSelected) {
                        sleep(mainLoopInterval);
                        if (clickImage(BwMatrixMeta.Metas.Character.Dialogs.loading)) {
                            debug("Character Loading!");
                            characterLoaded = true;
                            break;
                        }
                    } else {
                        debug("Selecting Character in Slot #" + characterSlot);

                        InteractionUtil.Mouse.mouseMoveAndClickAndHide(characterSelectionButtonOffsets[characterSlot-1].toScreenCoordinate());
                        sleep(mainLoopInterval);
                        if (clickImage(BwMatrixMeta.Metas.Character.Buttons.confirm)) {
                            info(ColorizeUtil.formatInfo, "\n\nCharacter Changed to Slot #" + characterSlot);
                            characterSelected = true;
                        }
                    }
                } else {
                    if (clickImage(BwMatrixMeta.Metas.Character.Labels.heroes)) {
                        loadedSelect = true;
                        debug("Character Select Menu Loaded");
                        sleep(mainLoopInterval);
                    }
                }
            }
            masterSwitch.set(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    @Override
    protected String getUsage() {
        return "<slot>";
    }

    @Override
    protected String getDescription() {
        return "Change character button. Used to switch between character slots";
    }

    @Override
    protected String getLimitationExplain() {
        return "This function only supports clicking the Character slots #1 #2 and #3 right now. Feel free to add support for more yourself.";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 2_000;
    }
}
