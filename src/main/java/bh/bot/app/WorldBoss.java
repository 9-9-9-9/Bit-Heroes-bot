package bh.bot.app;

import bh.bot.common.Telegram;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

public class WorldBoss extends AbstractApplication {
    private final AttendablePlace ap = AttendablePlaces.worldBoss;

    @Override
    protected void internalRun(String[] args) {

        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> loop(masterSwitch),
                () -> doClickTalk(masterSwitch::get),
                () -> detectDisconnected(masterSwitch),
                () -> autoExit(launchInfo.exitAfterXSecs, masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void loop(AtomicBoolean masterSwitch) {
        while (!masterSwitch.get()) {
            sleep(5_000);

            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss)) {
                debug("summonOnListingPartiesWorldBoss");
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses)) {
                debug("summonOnListingWorldBosses");
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty)) {
                debug("summonOnSelectingWorldBossTierAndAndDifficulty");
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss)) {
                debug("startBoss");
                continue;
            }

            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroup)) {
                debug("regroup");
                continue;
            }

            debug("confirmStartNotFullTeam");
            if (clickImage(BwMatrixMeta.Metas.WorldBoss.Dialogs.confirmStartNotFullTeam)) {
                InteractionUtil.Keyboard.sendSpaceKey();
                continue;
            }

            debug("None");
        }
    }

    @Override
    public String getAppCode() {
        return "world-boss";
    }

    @Override
    protected String getAppName() {
        return "BH-World Boss";
    }

    @Override
    protected String getScriptFileName() {
        return "world-boss";
    }

    @Override
    protected String getUsage() {
        return "";
    }

    @Override
    protected String getDescription() {
        return null;
    }

    @Override
    protected String getFlags() {
        return null;
    }

    @Override
    protected String getLimitationExplain() {
        return "This function is solo only and does not support select level or type of World Boss, only select by default So which boss do you want to hit? Choose it before turn this on";
    }
}
