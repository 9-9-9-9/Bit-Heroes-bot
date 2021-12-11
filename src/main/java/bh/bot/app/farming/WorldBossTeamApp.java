package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.ThreadUtil;
import org.fusesource.jansi.Ansi;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static bh.bot.Main.*;
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
        if (args.length != 0 && args.length != 1 && args.length != 3) {
            err("Wrong number of argument");
            exit(Main.EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS);
            return;
        }

        longTimeNoSee = Configuration.Timeout.longTimeNoSeeInMinutes * 60_000;
        int arg;
        try {
            arg = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            info(getHelp());
            arg = readInputLoopCount("How many times do you want to attack the world bosses?");
        }

        boolean skipConfirm = true;

        if (args.length == 3) {
            try {
                minimumNumberOfTeamMembers = Integer.parseInt(args[1]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                err(ex);
                err("Bad value of 2nd argument (minimum number of team members)");
                Main.exit(EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
                return;
            }
            try {
                maximumNumberOfTeamMembers = Integer.parseInt(args[2]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                err(ex);
                err("Bad value of 3rd argument (Number of team members that the target World Boss supports)");
                Main.exit(EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
                return;
            }
        } else {
            minimumNumberOfTeamMembers = readInputMinimumTeamMembersCount();
            maximumNumberOfTeamMembers = readInputMaximumTeamMembersCount();
            skipConfirm = false;
        }

        if (minimumNumberOfTeamMembers < 2 || minimumNumberOfTeamMembers > 5) {
            err("Minimum number of team members required to start is %d, which is a wrong number, valid range is from 2 to 5", maximumNumberOfTeamMembers);
            Main.exit(Main.EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
            return;
        }

        if (maximumNumberOfTeamMembers < 3 || maximumNumberOfTeamMembers > 5) {
            err("Number of team members supported by World Boss is %d, which is a wrong number, valid range is from 3 to 5", maximumNumberOfTeamMembers);
            info("Netherworld, 3XT3RM1N4T10N, Brimstone Syndicate, Titans Attack, The Ignited Abyss support 3 team members");
            info("Melvin Factory, Nordic Dream support 4 team members");
            info("Orlag Clan supports 5 team members");
            Main.exit(Main.EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
            return;
        }

        if (minimumNumberOfTeamMembers > maximumNumberOfTeamMembers) {
            err("The minimum number of team members you've set is %d which is greater than maximum number of slots the target world boss supports is %d", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
            Main.exit(Main.EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
            return;
        }

        if (skipConfirm) {
            if (minimumNumberOfTeamMembers < maximumNumberOfTeamMembers) {
                info("You configured your team must have at least %d/%d members in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
            } else {
                info("You configured your team must be full %d/%d members in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
            }
        } else {
            if (!readYesNoInput(String.format("The World Boss you are going to hunt, supports %d members. Is that correct?", maximumNumberOfTeamMembers), "If something wrong, please press N and restart bot and provide correct configuration, otherwise bot won't work correctly", false, true)) {
                Main.exit(EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
                return;
            }

            String ask;
            if (minimumNumberOfTeamMembers < maximumNumberOfTeamMembers) {
                ask = String.format("You configured your team must have at least %d/%d members in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
            } else {
                ask = String.format("You configured your team must be full %d/%d members in order to Start", minimumNumberOfTeamMembers, maximumNumberOfTeamMembers);
            }

            if (!readYesNoInput(ask + ". Is that correct?", "If something wrong, please press N and restart bot and provide correct configuration, otherwise bot won't work correctly", false, true)) {
                Main.exit(EXIT_CODE_INCORRECT_RUNTIME_CONFIGURATION);
                return;
            }
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
        final Point coordinateHideMouse = new Point(0, 0);

        info(ColorizeUtil.formatInfo, "\n\nStarting World Boss (Team)");
        warningWatch(ColorizeUtil.formatError);
        warningWatch(ColorizeUtil.formatWarning);
        warningWatch(ColorizeUtil.formatInfo);
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

                if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.confirmStartNotFullTeam)) {
                    debug("confirmStartNotFullTeam");
                    InteractionUtil.Keyboard.sendSpaceKey();
                    moveCursor(coordinateHideMouse);
                    continue;
                }

                if (findImage(BwMatrixMeta.Metas.WorldBoss.Dialogs.notEnoughXeals) != null) {
                    debug("notEnoughXeals");
                    InteractionUtil.Keyboard.sendEscape();
                    break;
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

    private void warningWatch(Function<Ansi, Ansi> ansiFormat) {
        info(ansiFormat, "*** NOTICE: REMEMBER YOU HAVE TO WATCH/CHECK THE GAME SOMETIME TO PREVENT UN-EXPECTED HANG/LOSS DUE TO UN-MANAGED BEHAVIORS LIKE MISSING MEMBERS, RE-GROUP FAILED, INCORRECT GROUP MATCHING...ETC ***");
    }

    @Override
    protected String getUsage() {
        return "<loop_count> <minimum_number_of_team_members> <number_of_team_members_the_world_boss_supports>";
    }

    @Override
    public String getArgHint() {
        return "You can either provide no argument or 1 (loop count) or 3 arguments (<loop_count> <minimum_number_of_team_members> <number_of_team_members_the_world_boss_supports>). Notice about the 3rd argument <number_of_team_members_the_world_boss_supports>, it is how many team members the target world boss supports (eg 3 for Titans Attack, 5 for Orlag Clan). If you want to start World Boss Team for 50 turns, only start when having at least 2 members and the target World Boss only supports 4 members then fill it: \"50 2 4\"";
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

    protected int readInputMinimumTeamMembersCount() {
        return readInput("Minimum number of team members required to Start? (otherwise bot will wait)", "If you want to wait until your team has at least 3 members before start hunting world boss, fill it 3 here, etc...", s -> {
            try {
                int num = Integer.parseInt(s);
                if (num < 1)
                    return new Tuple3<>(false, "Must greater than 1", 0);
                else if (num == 1)
                    return new Tuple3<>(false, "Come on, this is world boss team, why 1? Use World Boss Solo instead!", 0);
                else if (num > 5)
                    return new Tuple3<>(false, "Must lower than 6", 0);
                return new Tuple3<>(true, null, num);
            } catch (NumberFormatException ex1) {
                return new Tuple3<>(false, "It's not a number", 0);
            }
        });
    }

    protected int readInputMaximumTeamMembersCount() {
        return readInput("How many team members does the target World Boss supports?\n" +
                        "  3 (Netherworld, 3XT3RM1N4T10N, Brimstone Syndicate, Titans Attack, The Ignited Abyss)\n" +
                        "  4 (Melvin Factory, Nordic Dream)\n" +
                        "  5 (Orlag Clan)", null, s -> {
            try {
                int num = Integer.parseInt(s);
                if (num < 3) {
                    return new Tuple3<>(false, "Must greater than 2", 0);
                }
                else if (num > 5) {
                    return new Tuple3<>(false, "Must lower than 6", 0);
                }
                return new Tuple3<>(true, null, num);
            } catch (NumberFormatException ex1) {
                return new Tuple3<>(false, "It's not a number", 0);
            }
        });
    }
}
