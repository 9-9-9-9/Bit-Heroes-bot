package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.ThreadUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseMoveAndClickAndHide;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppMeta(code = "world-boss-team", name = "World Boss (Team)", displayOrder = 6.1)
@RequireSingleInstance
public class WorldBossTeamApp extends AbstractApplication {
    // TODO check usage
    private int longTimeNoSee = Configuration.Timeout.defaultLongTimeNoSeeInMinutes * 60_000;

    private int minimumNumberOfTeamMembers;
    private int maximumNumberOfTeamMembers;

    @Override
    protected void internalRun(String[] args) {
        longTimeNoSee = Configuration.Timeout.longTimeNoSeeInMinutes * 60_000;
        int arg;
        try {
            arg = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            info(getHelp());
            arg = readInputLoopCount("How many times do you want to attack the world bosses?");
        }

        minimumNumberOfTeamMembers = readInputMinimumTeamMembersCount();

        final Tuple2<Byte, Byte> woldBossLevelRange = UserConfig.getWorldBossLevelRange();
        info("All World Boss levels:");
        for (int rl = woldBossLevelRange._1; rl <= woldBossLevelRange._2; rl++)
            info(String.format("  %2d. %s\n", rl, UserConfig.getWorldBossLevelDesc(rl)));

        int worldBossLevel = readInputWorldBossLevel(woldBossLevelRange._1, woldBossLevelRange._2);
        maximumNumberOfTeamMembers = extractMaximumTeamMemberCountFromWorldBossLevel(worldBossLevel);
        if (maximumNumberOfTeamMembers < 3 || maximumNumberOfTeamMembers > 5) {
            err("I'm sorry, this world boss level has not been supported yet, please raise an Issue ticket at my github");
            Main.exit(Main.EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
            return;
        }

        info("%s supports up to %d team members (if I'm wrong, please raise an Issue ticket at my github)", UserConfig.getWorldBossLevelDesc(worldBossLevel), maximumNumberOfTeamMembers);
        if (minimumNumberOfTeamMembers == maximumNumberOfTeamMembers) {
            info("and you configured your team must be full in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
        } else if (minimumNumberOfTeamMembers < maximumNumberOfTeamMembers) {
            info("and you configured your team must have at least %d/%d members in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
        } else {
            err("The minimum number of team members you've inputted is %d which is greater than number of slots %s supports is %d", minimumNumberOfTeamMembers, UserConfig.getWorldBossLevelDesc(worldBossLevel), maximumNumberOfTeamMembers);
            Main.exit(Main.EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
            return;
        }

        final int loop = arg;
        Log.info("Loop: %d", loop);
        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        ThreadUtil.waitDone(
                () -> doLoopClickImage(loop, masterSwitch),
                () -> internalDoSmallTasks( //
                        masterSwitch, //
                        SmallTasks //
                                .builder() //
                                .clickTalk() //
                                .clickDisconnect() //
                                .reactiveAuto() //
                                .autoExit() //
                                .build() //
                ), //
                () -> doCheckGameScreenOffset(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void doLoopClickImage(int loopCount, AtomicBoolean masterSwitch) {
        info(ColorizeUtil.formatInfo, "\n\nStarting World Boss (Team)");
        info(ColorizeUtil.formatError, "*** NOTICE: REMEMBER YOU HAVE TO WATCH/CHECK THE GAME SOMETIME TO PREVENT UN-EXPECTED HANG/LOSS DUE TO UN-MANAGED BEHAVIORS LIKE MISSING MEMBERS,...ETC ***");
        try {
            final int mainLoopInterval = Configuration.Interval.Loop.getMainLoopInterval(getDefaultMainLoopInterval());

            BwMatrixMeta[] inviteButtons = new BwMatrixMeta[maximumNumberOfTeamMembers];
            inviteButtons[0] = BwMatrixMeta.Metas.WorldBoss.Buttons.invite1;
            if (maximumNumberOfTeamMembers >= 2)
                inviteButtons[1] = BwMatrixMeta.Metas.WorldBoss.Buttons.invite2;
            if (maximumNumberOfTeamMembers >= 3)
                inviteButtons[2] = BwMatrixMeta.Metas.WorldBoss.Buttons.invite3;
            if (maximumNumberOfTeamMembers >= 4)
                inviteButtons[3] = BwMatrixMeta.Metas.WorldBoss.Buttons.invite4;
            if (maximumNumberOfTeamMembers >= 5)
                inviteButtons[4] = BwMatrixMeta.Metas.WorldBoss.Buttons.invite5;

            moveCursor(new Point(950, 100));
            long lastRound = System.currentTimeMillis();
            while (loopCount > 0 && !masterSwitch.get()) {
                sleep(mainLoopInterval);
                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroup)) {
                    loopCount--;
                    lastRound = System.currentTimeMillis();
                    info("%d loop left", loopCount);
                    continue;
                }

                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroupOnDefeated)) {
                    loopCount--;
                    lastRound = System.currentTimeMillis();
                    info("%d loop left (you was defeated this round)", loopCount);
                    Telegram.sendMessage("Defeated in World Boss (Team)", true);
                    continue;
                }

                if (System.currentTimeMillis() - lastRound > longTimeNoSee) {
                    info("Long time no see => Stop");
                    Telegram.sendMessage("long time no see button", true);
                    break;
                }

                mouseMoveAndClickAndHide(Configuration.screenResolutionProfile.getOffsetLabelWorldBossInSummonDialog().toScreenCoordinate());

                if (findImage(BwMatrixMeta.Metas.WorldBoss.Buttons.unready) != null) {
                    debug("Team member (already clicked the Ready button) => waiting");
                    sleep(4_000);
                    continue;
                }

                if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.ready)) {
                    debug("Team member => waiting");
                    continue;
                }

                if (findImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss) == null) {
                    // at this point, not sure about if we're in the ready-to-start screen but that's not matter, no thing to do at this point for sure
                    debug("Team leader but not all members are ready => waiting");
                    continue;
                }

                if (minimumNumberOfTeamMembers < maximumNumberOfTeamMembers) {
                    // partially
                    int cntInviteButtons = 0;
                    for (BwMatrixMeta inviteButton : inviteButtons) {
                        if (findImage(inviteButton) != null) {
                            cntInviteButtons += 1;
                        }
                    }

                    if (cntInviteButtons > maximumNumberOfTeamMembers - minimumNumberOfTeamMembers) {
                        info("%d/%d members, waiting for other team members to join", maximumNumberOfTeamMembers - cntInviteButtons, maximumNumberOfTeamMembers);
                    } else {
                        if (!clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss)) {
                            warn("Unknown state, can't detect the Start button");
                        }
                    }

                } else {
                    // full team
                    boolean foundAnyInviteButton = false;
                    for (BwMatrixMeta inviteButton : inviteButtons) {
                        if (findImage(inviteButton) != null) {
                            foundAnyInviteButton = true;
                            break;
                        }
                    }

                    if (foundAnyInviteButton) {
                        info("Team is not full, waiting for team members");
                    } else {
                        if (!clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss)) {
                            warn("Unknown state, can't detect the Start button");
                        }
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
        return "<count>";
    }

    @Override
    protected String getDescription() {
        return "Do World Boss in team";
    }

    @Override
    protected String getLimitationExplain() {
        return "World Boss is for team only (need more than 1 person) thus it's a bit complicated and is recommended to watch the bot in case bot is malfunctioned";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 1_800;
    }

    private int extractMaximumTeamMemberCountFromWorldBossLevel(int worldBossLevel) {
        switch (worldBossLevel)
        {
            //noinspection SpellCheckingInspection
            case 1: // Orlag Clan
                return 5;
            case 2: // Netherworld
                return 3;
            case 3: // Melvin Factory
                return 4;
            case 4: // 3XT3RM1N4T10N
                return 3;
            case 5: // Brimstone Syndicate
                return 3;
            case 6: // Titans Attack
                return 3;
            case 7: // The Ignited Abyss
                return 3;
            case 8: // Nordic Dream
                return 4;
            default:
                return -1;
        }
    }

    protected int readInputMinimumTeamMembersCount() {
        return readInput("How many team members are required to start World Boss? (fill it exactly)", "Numeric only", s -> {
            try {
                int num = Integer.parseInt(s);
                if (num < 2) {
                    return new Tuple3<>(false, "Must greater than 1", 0);
                }
                return new Tuple3<>(true, null, num);
            } catch (NumberFormatException ex1) {
                return new Tuple3<>(false, "The value you inputted is not a number", 0);
            }
        });
    }

    protected int readInputWorldBossLevel(int min, int max) {
        return readInput("Specific World Boss level to farm with team? (to be used to extract maximum number of team members)", "Numeric only", s -> {
            try {
                int num = Integer.parseInt(s);
                if (num < min) {
                    return new Tuple3<>(false, "Minimum is " + min, 0);
                }
                if (num > max) {
                    return new Tuple3<>(false, "Maximum is " + max, 0);
                }
                return new Tuple3<>(true, null, num);
            } catch (NumberFormatException ex1) {
                return new Tuple3<>(false, "The value you inputted is not a number", 0);
            }
        });
    }
}
