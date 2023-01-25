package bh.bot.app;

import static bh.bot.common.Log.info;
import static bh.bot.common.Log.debug;
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

@AppMeta(code = "character", name = "Change Character", displayOrder = 1, argType = "number", argAsk = "What character slot do you want to pick?", argDefault = "1", argRequired = true)
@RequireSingleInstance
public class ChangeCharacterApp extends AbstractApplication {

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
        info("Character in slot %3d ", characterSlot);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoopClickImage(characterSlot, masterSwitch),
                () -> internalDoSmallTasks( //
                        masterSwitch, //
                        SmallTasks //
                                .builder() //
                                .clickTalk() //
                                .clickDisconnect() //
                                .reactiveAuto() //
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
                        if (characterSlot == 1) {
                            InteractionUtil.Mouse.mouseMoveAndClickAndHide(new Offset(250, 200).toScreenCoordinate());
                        } else if (characterSlot == 2) {
                            InteractionUtil.Mouse.mouseMoveAndClickAndHide(new Offset(375, 200).toScreenCoordinate());
                        } else if (characterSlot == 3) {
                            InteractionUtil.Mouse.mouseMoveAndClickAndHide(new Offset(500, 200).toScreenCoordinate());
                        } else {
                            throw new NotSupportedException("Cannot select character in this slot.");
                        }
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