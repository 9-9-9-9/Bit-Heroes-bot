package bh.bot.common.types;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.Configuration.Size;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.tuples.Tuple3;

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
    public abstract Tuple3<Integer, Integer, Integer> getBackwardScanRightEvents();

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
        public Tuple3<Integer, Integer, Integer> getBackwardScanRightEvents() {
            return new Tuple3<>(725, 433, 72);
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
        public Tuple3<Integer, Integer, Integer> getBackwardScanRightEvents() {
            throw NotSupportedException.steam();
        }
    }
}
