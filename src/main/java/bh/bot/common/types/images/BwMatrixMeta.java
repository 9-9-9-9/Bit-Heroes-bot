package bh.bot.common.types.images;

import bh.bot.common.Configuration;
import bh.bot.common.utils.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class BwMatrixMeta {
    private final ArrayList<int[]> blackPixels;
    private final ArrayList<int[]> nonBlackPixels;
    private final int w;
    private final int h;
    private final int blackPixelRgb;
    private final ImageUtil.DynamicRgb blackPixelDRgb;
    private final Configuration.Offset coordinateOffset;
    private int[] lastMatch = new int[]{-1, -1};

    public BwMatrixMeta(BufferedImage img, Configuration.Offset coordinateOffset, int blackPixelRgb) {
        final int matrixPointColorPixelRgb = 0x000000;
        final int anyColorPixelRgb = 0xFFFFFF;
        try {
            this.coordinateOffset = coordinateOffset;
            this.blackPixelRgb = blackPixelRgb & 0xFFFFFF;
            this.blackPixelDRgb = new ImageUtil.DynamicRgb(this.blackPixelRgb, Configuration.Tolerant.colorBw);
            blackPixels = new ArrayList<>();
            nonBlackPixels = new ArrayList<>();
            w = img.getWidth();
            h = img.getHeight();
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int rgb = img.getRGB(x, y) & 0xFFFFFF;
                    if (rgb == matrixPointColorPixelRgb)
                        blackPixels.add(new int[]{x, y});
                    else if (rgb != anyColorPixelRgb)
                        nonBlackPixels.add(new int[]{x, y});
                }
            }
        } finally {
            img.flush();
        }
    }

    public boolean isMatchBlackRgb(int rgb) {
        return ImageUtil.areColorsSimilar(blackPixelDRgb, rgb, Configuration.Tolerant.color);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getBlackPixelRgb() {
        return blackPixelRgb;
    }

    public ImageUtil.DynamicRgb getBlackPixelDRgb() {
        return blackPixelDRgb;
    }

    public ArrayList<int[]> getBlackPixels() {
        return blackPixels;
    }

    public ArrayList<int[]> getNonBlackPixels() {
        return nonBlackPixels;
    }

    public void setLastMatchPoint(int x, int y) {
        lastMatch = new int[]{x, y};
    }

    public int[] getLastMatchPoint() {
        return lastMatch;
    }

    public Configuration.Offset getCoordinateOffset() {
        return coordinateOffset;
    }

    public static class Metas {
        public static class Globally {
            public static class Buttons {
                public static BwMatrixMeta talkRightArrow;
                public static BwMatrixMeta rerun;
                public static BwMatrixMeta reconnect;
            }
        }

        public static class Dungeons {
            public static class Buttons {
                public static BwMatrixMeta rerun;
            }
        }

        public static class Fishing {
            public static class Buttons {
                public static BwMatrixMeta start;
                public static BwMatrixMeta cast;
                public static BwMatrixMeta catch_;
            }

            public static class Labels {
                public static BwMatrixMeta fishing;
            }
        }

        public static class WorldBoss {
            public static class Buttons {
                public static BwMatrixMeta summonOnListingPartiesWorldBoss;
                public static BwMatrixMeta summonOnListingWorldBosses;
                public static BwMatrixMeta summonOnSelectingWorldBossTierAndAndDifficulty;
                public static BwMatrixMeta startBoss;
                public static BwMatrixMeta regroup;
                public static BwMatrixMeta regroupOnDefeated;
            }

            public static class Dialogs {
                public static BwMatrixMeta confirmStartNotFullTeam;
                public static BwMatrixMeta notEnoughXeals;
            }
        }
    }

    public static void load() throws IOException {
        Metas.Globally.Buttons.talkRightArrow = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/globally.talkRightArrow-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTalkRightArrow(),
                0x000000
        );
        Metas.Globally.Buttons.reconnect = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/globally.reconnect-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonReconnect(),
                0xFFFFFF
        );
        Metas.Dungeons.Buttons.rerun = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/dungeons.rerun-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonDungeonReRun(),
                0xFFFFFF);
        Metas.Fishing.Labels.fishing = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "labels/fishing-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetLabelFishing(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.start = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/fishing.start-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingStart(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.cast = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/fishing.cast-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingCast(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.catch_ = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/fishing.catch-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingCatch(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.summon-party-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingPartiesWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnListingWorldBosses = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.summon-boss-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingWorldBosses(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.summon-tier-diff-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.startBoss = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.start-boss.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonStartWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroup = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.regroup.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonRegroupWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroupOnDefeated = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/world-boss.regroup.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonRegroupAfterDefeatByWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Dialogs.confirmStartNotFullTeam = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "dialogs/world-boss.confirm-start-not-full-team.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogStartWorldBossWithoutFullTeam(),
                0xFFFFFF
        );
        Metas.WorldBoss.Dialogs.notEnoughXeals = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "dialogs/world-boss.not-enough-xeals.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughXeals(),
                0xFFFFFF
        );
    }
}