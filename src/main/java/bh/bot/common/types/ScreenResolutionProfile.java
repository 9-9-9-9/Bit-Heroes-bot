package bh.bot.common.types;

import bh.bot.common.Configuration;

public abstract class ScreenResolutionProfile {
    public abstract String getName();
    public abstract int getSupportedGameResolutionWidth();
    public abstract int getSupportedGameResolutionHeight();
    public abstract Configuration.Offset getOffsetButtonDungeonReRun();
    public abstract Configuration.Offset getOffsetButtonTalkRightArrow();
    public abstract Configuration.Offset getOffsetButtonReconnect();
    public abstract Configuration.Offset getOffsetLabelFishing();
    public abstract Configuration.Offset getOffsetButtonFishingStart();
    public abstract Configuration.Offset getOffsetButtonFishingCast();
    public abstract Configuration.Offset getOffsetButtonFishingCatch();
    public abstract Configuration.Offset getOffsetDetect100PcCatchingFish();
    public abstract Configuration.Offset getOffsetScanCastingFish();
    public abstract Configuration.Size getScanSizeCastingFish();

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
        public Configuration.Offset getOffsetButtonDungeonReRun() {
            return new Configuration.Offset(307, 467);
        }

        @Override
        public Configuration.Offset getOffsetButtonTalkRightArrow() {
            return new Configuration.Offset(718, 287);
        }

        @Override
        public Configuration.Offset getOffsetButtonReconnect() {
            return new Configuration.Offset(343, 352);
        }

        @Override
        public Configuration.Offset getOffsetLabelFishing() {
            return new Configuration.Offset(356, 12);
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingStart() {
            return new Configuration.Offset(363, 464);
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingCast() {
            return new Configuration.Offset(371, 462);
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingCatch() {
            return new Configuration.Offset(363, 464);
        }

        @Override
        public Configuration.Offset getOffsetDetect100PcCatchingFish() {
            return new Configuration.Offset(697, 389);
        }

        @Override
        public Configuration.Offset getOffsetScanCastingFish() {
            return new Configuration.Offset(583, 356);
        }

        @Override
        public Configuration.Size getScanSizeCastingFish() {
            return new Configuration.Size(48, 72);
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
        public Configuration.Offset getOffsetButtonDungeonReRun() {
            return new Configuration.Offset(311, 433); //
        }

        @Override
        public Configuration.Offset getOffsetButtonTalkRightArrow() {
            return new Configuration.Offset(700, 268); //
        }

        @Override
        public Configuration.Offset getOffsetButtonReconnect() {
            return new Configuration.Offset(346, 326); //
        }

        @Override
        public Configuration.Offset getOffsetLabelFishing() {
            return new Configuration.Offset(358, 12); //
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingStart() {
            return new Configuration.Offset(362, 426);
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingCast() {
            return new Configuration.Offset(370, 426);
        }

        @Override
        public Configuration.Offset getOffsetButtonFishingCatch() {
            return new Configuration.Offset(362, 426);
        }

        @Override
        public Configuration.Offset getOffsetDetect100PcCatchingFish() {
            return new Configuration.Offset(685, 357); //
        }

        @Override
        public Configuration.Offset getOffsetScanCastingFish() {
            return new Configuration.Offset(572, 326); //
        }

        @Override
        public Configuration.Size getScanSizeCastingFish() {
            return new Configuration.Size(45, 68); //
        }
    }
}
