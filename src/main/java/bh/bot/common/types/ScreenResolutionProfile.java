package bh.bot.common.types;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.Configuration.Size;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.tuples.Tuple4;

public abstract class ScreenResolutionProfile {
    public abstract String getName();
    public abstract int getSupportedGameResolutionWidth();
    public abstract int getSupportedGameResolutionHeight();
    public abstract Offset getOffsetButtonDungeonReRun();
    public abstract Offset getOffsetButtonTalkRightArrow();
    public abstract Offset getOffsetButtonReconnect();
    public abstract Offset getOffsetLabelFishing();
    public abstract Offset getOffsetButtonFishingStart();
    public abstract Offset getOffsetButtonFishingCast();
    public abstract Offset getOffsetButtonFishingCatch();
    public abstract Offset getOffsetDetect100PcCatchingFish();
    public abstract Offset getOffsetScanCastingFish();
    public abstract Size getScanSizeCastingFish();
    public abstract Tuple4<Integer, Integer, Integer, Integer> getBackwardScanRightSideAttendablePlaces(); // min x, max y, step backward, max x
    public abstract Tuple4<Integer, Integer, Integer, Integer> getBackwardScanLeftSideAttendablePlaces(); // min x, min y, step forward, max x
    public abstract Offset getOffsetButtonSummonOnListingPartiesWorldBoss();
    public abstract Offset getOffsetButtonSummonOnListingWorldBosses();
    public abstract Offset getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty();
    public abstract Offset getOffsetButtonStartWorldBoss();
    public abstract Offset getOffsetDialogStartWithoutFullTeam();
    public abstract Offset getOffsetButtonRegroupWorldBoss();
    public abstract Offset getOffsetButtonRegroupAfterDefeatByWorldBoss();
    public abstract Offset getOffsetDialogNotEnoughXeals();
    public abstract Offset getOffsetButtonPlayPvpArena();
    public abstract Offset getOffsetButtonFight1PvpArena();
    public abstract Offset getOffsetButtonAcceptPvpArena();
    public abstract Offset getOffsetButtonBackToTownFromPvpArenaOnWin();
    public abstract Offset getOffsetButtonBackToTownFromPvpArenaOnLose();
    public abstract Offset getOffsetDialogNotEnoughPvpTicket();


    public static class WebProfile extends ScreenResolutionProfile {

        @Override
        public String getName() {
            return "web";
        }

        @Override
        public int getSupportedGameResolutionWidth() {
            return 800;
        }

        @Override
        public int getSupportedGameResolutionHeight() {
            return 520;
        }

        @Override
        public Offset getOffsetButtonDungeonReRun() {
            return new Offset(309, 468);
        }

        @Override
        public Offset getOffsetButtonTalkRightArrow() {
            return new Offset(718, 287);
        }

        @Override
        public Offset getOffsetButtonReconnect() {
            return new Offset(344, 353);
        }

        @Override
        public Offset getOffsetLabelFishing() {
            return new Offset(355, 13);
        }

        @Override
        public Offset getOffsetButtonFishingStart() {
            return new Offset(362, 464);
        }

        @Override
        public Offset getOffsetButtonFishingCast() {
            return new Offset(370, 464);
        }

        @Override
        public Offset getOffsetButtonFishingCatch() {
            return new Offset(362, 464);
        }

        @Override
        public Offset getOffsetDetect100PcCatchingFish() {
            return new Offset(697, 389);
        }

        @Override
        public Offset getOffsetScanCastingFish() {
            return new Offset(583, 356);
        }

        @Override
        public Size getScanSizeCastingFish() {
            return new Size(48, 72);
        }

        @Override
        public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanRightSideAttendablePlaces() {
            return new Tuple4<>(730, 433, -72, 789);
        }

        @Override
        public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanLeftSideAttendablePlaces() {
            return new Tuple4<>(14, 96, 72, 59);
        }

        @Override
        public Offset getOffsetButtonSummonOnListingPartiesWorldBoss() {
            return new Offset(452, 451);
        }

        @Override
        public Offset getOffsetButtonSummonOnListingWorldBosses() {
            return new Offset(495, 368);
        }

        @Override
        public Offset getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty() {
            return new Offset(432, 394);
        }

        @Override
        public Offset getOffsetButtonStartWorldBoss() {
            return new Offset(342, 460);
        }

        @Override
        public Offset getOffsetDialogStartWithoutFullTeam() {
            return new Offset(279, 221);
        }

        @Override
        public Offset getOffsetButtonRegroupWorldBoss() {
            return new Offset(368, 468);
        }

        @Override
        public Offset getOffsetButtonRegroupAfterDefeatByWorldBoss() {
            return new Offset(293, 467);
        }

        @Override
        public Offset getOffsetDialogNotEnoughXeals() {
            return new Offset(285, 231);
        }

        @Override
        public Offset getOffsetButtonPlayPvpArena() {
            return new Offset(520, 266);
        }

        @Override
        public Offset getOffsetButtonFight1PvpArena() {
            return new Offset(575, 198);
        }

        @Override
        public Offset getOffsetButtonAcceptPvpArena() {
            return new Offset(474, 457);
        }

        @Override
        public Offset getOffsetButtonBackToTownFromPvpArenaOnWin() {
            return new Offset(462, 468);
        }

        @Override
        public Offset getOffsetButtonBackToTownFromPvpArenaOnLose() {
            return new Offset(387, 468);
        }

        @Override
        public Offset getOffsetDialogNotEnoughPvpTicket() {
            return new Offset(276, 231);
        }
    }

    public static class SteamProfile extends ScreenResolutionProfile {

        @Override
        public String getName() {
            return "steam";
        }

        @Override
        public int getSupportedGameResolutionWidth() {
            return 800;
        }

        @Override
        public int getSupportedGameResolutionHeight() {
            return 480;
        }

        @Override
        public Offset getOffsetButtonDungeonReRun() {
            return new Offset(312, 435); //
        }

        @Override
        public Offset getOffsetButtonTalkRightArrow() {
            return new Offset(697, 265); //
        }

        @Override
        public Offset getOffsetButtonReconnect() {
            return new Offset(348, 327); //
        }

        @Override
        public Offset getOffsetLabelFishing() {
            return new Offset(358, 12);  //
        }

        @Override
        public Offset getOffsetButtonFishingStart() {
            return new Offset(364, 427); //
        }

        @Override
        public Offset getOffsetButtonFishingCast() {
            return new Offset(372, 427); //
        }

        @Override
        public Offset getOffsetButtonFishingCatch() {
            return new Offset(364, 427); //
        }

        @Override
        public Offset getOffsetDetect100PcCatchingFish() {
            return new Offset(685, 357); //
        }

        @Override
        public Offset getOffsetScanCastingFish() {
            return new Offset(572, 326);  //
        }

        @Override
        public Size getScanSizeCastingFish() {
            return new Size(45, 68); //
        }

        @Override
        public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanRightSideAttendablePlaces() {
            throw NotSupportedException.steam();
        }

        @Override
        public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanLeftSideAttendablePlaces() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonSummonOnListingPartiesWorldBoss() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonSummonOnListingWorldBosses() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonStartWorldBoss() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetDialogStartWithoutFullTeam() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonRegroupWorldBoss() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonRegroupAfterDefeatByWorldBoss() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetDialogNotEnoughXeals() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonPlayPvpArena() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonFight1PvpArena() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonAcceptPvpArena() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonBackToTownFromPvpArenaOnWin() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetButtonBackToTownFromPvpArenaOnLose() {
            throw NotSupportedException.steam();
        }

        @Override
        public Offset getOffsetDialogNotEnoughPvpTicket() {
            throw NotSupportedException.steam();
        }
    }
}
