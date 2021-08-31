package bh.bot.common.types.images;

import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.StringUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.ImageUtil.freeMem;

public class BwMatrixMeta {
    private final int[] firstBlackPixelOffset;
    private final ArrayList<int[]> blackPixels;
    private final ArrayList<int[]> nonBlackPixels;
    private final int w;
    private final int h;
    private final int blackPixelRgb;
    private final ImageUtil.DynamicRgb blackPixelDRgb;
    private final Configuration.Offset coordinateOffset;
    private final int[] lastMatch = new int[]{-1, -1};
    private final byte tolerant;
    private final String imageNameCode;
    private final boolean notAvailable;

    public BwMatrixMeta(BufferedImageInfo bii, Configuration.Offset coordinateOffset, int blackPixelRgb) {
        if (bii.notAvailable) {
            this.firstBlackPixelOffset = null;
            this.blackPixels = null;
            this.nonBlackPixels = null;
            this.w = 0;
            this.h = 0;
            this.blackPixelRgb = 0;
            this.blackPixelDRgb = null;
            this.coordinateOffset = null;
            this.tolerant = -1;
            this.imageNameCode = bii.code;
            this.notAvailable = true;
        } else {
            String customTolerantKey = "tolerant.color.bw|" + bii.code;
            try {
                byte tolerant = Configuration.Tolerant.colorBw;
                String value = Configuration.read(customTolerantKey);
                if (StringUtil.isNotBlank(value)) {
                    tolerant = Byte.parseByte(value);
                    if (tolerant < 0)
                        throw new InvalidDataException("Invalid value of configuration key '%s': must be a positive number", customTolerantKey);
                    if (tolerant > Configuration.Tolerant.colorBw)
                        throw new InvalidDataException("Invalid value of configuration key '%s': must not greater than value of 'tolerant.color.bw' key (which is %d)", customTolerantKey, Configuration.Tolerant.colorBw);
                    if (tolerant != Configuration.Tolerant.colorBw)
                        dev("Tolerant overrided to %d by key `%s`", tolerant, customTolerantKey);
                }
                this.tolerant = tolerant;
            } catch (NumberFormatException ex) {
                throw new InvalidDataException("Invalid format of key '%s': must be a number, valid value within range from 0 to %d", customTolerantKey, Configuration.Tolerant.colorBw);
            }

            final int matrixPointColorPixelRgb = 0x000000;
            final int anyColorPixelRgb = 0xFFFFFF;
            BufferedImage img = bii.bufferedImage;
            try {
                this.coordinateOffset = coordinateOffset;
                this.blackPixelRgb = blackPixelRgb & 0xFFFFFF;
                this.blackPixelDRgb = new ImageUtil.DynamicRgb(this.blackPixelRgb, this.tolerant);
                this.blackPixels = new ArrayList<>();
                this.nonBlackPixels = new ArrayList<>();
                this.w = img.getWidth();
                this.h = img.getHeight();
                for (int y = 0; y < img.getHeight(); y++) {
                    for (int x = 0; x < img.getWidth(); x++) {
                        int rgb = img.getRGB(x, y) & 0xFFFFFF;
                        if (rgb == matrixPointColorPixelRgb)
                            this.blackPixels.add(new int[]{x, y});
                        else if (rgb != anyColorPixelRgb)
                            this.nonBlackPixels.add(new int[]{x, y});
                    }
                }
                this.firstBlackPixelOffset = this.blackPixels.get(0);
            } finally {
                freeMem(img);
            }

            this.imageNameCode = bii.code;
            this.notAvailable = false;
        }
    }

    public void throwIfNotAvailable() {
        if (this.notAvailable) {
            err("Image is not available: %s", this.imageNameCode);
            throw new NotSupportedException(String.format("Image is not available for profile '--%s': %s", Configuration.profileName, this.imageNameCode));
        }
    }

    public int[] getFirstBlackPixelOffset() {
        return firstBlackPixelOffset;
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
        lastMatch[0] = x;
        lastMatch[1] = y;
    }

    public int[] getLastMatchPoint() {
        return lastMatch;
    }

    public int getTolerant() {
        return tolerant;
    }

    public Configuration.Offset getCoordinateOffset() {
        return coordinateOffset;
    }

    public String getImageNameCode() {
        return imageNameCode;
    }

    public static class Metas {
        public static class Globally {
            public static class Buttons {
                public static BwMatrixMeta talkRightArrow;
                public static BwMatrixMeta rerun;
                public static BwMatrixMeta reconnect;
                public static BwMatrixMeta autoG;
                public static BwMatrixMeta autoR;
                public static BwMatrixMeta radioButton;
                public static BwMatrixMeta close;
            }

            public static class Dialogs {
                public static BwMatrixMeta confirmQuitBattle;
                public static BwMatrixMeta confirmStartNotFullTeam;
                public static BwMatrixMeta areYouStillThere;
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
                public static BwMatrixMeta notEnoughXeals;
            }
        }

        public static class PvpArena {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta fight1;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta townOnWin;
                public static BwMatrixMeta townOnLose;
            }

            public static class Dialogs {
                public static BwMatrixMeta notEnoughTicket;
            }
        }

        public static class Invasion {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta town;
            }

            public static class Dialogs {
                public static BwMatrixMeta notEnoughBadges;
            }
        }

        public static class Trials {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta town;
            }

            public static class Dialogs {
                public static BwMatrixMeta notEnoughTokens;
            }
        }

        public static class Gvg {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta fight;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta town;
            }
        }

        public static class Gauntlet {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta town;
            }
        }

        public static class Raid {
            public static class Buttons {
                public static BwMatrixMeta town;
                public static BwMatrixMeta summon;
            }

            public static class Labels {
                public static BwMatrixMeta labelInSummonDialog;
            }
        }
    }

    public static void load() throws IOException {
        Metas.Globally.Buttons.talkRightArrow = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.talkRightArrow-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTalkRightArrow(),
                0x000000
        );
        Metas.Globally.Buttons.reconnect = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.reconnect-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonReconnect(),
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoG = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.auto-green-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAuto(),
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoR = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.auto-red-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAuto(),
                0xFFFFFF
        );
        Metas.Globally.Buttons.radioButton = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.radio-button-mx.bmp"
                ), //
                Configuration.Offset.none(),
                0x000000
        );
        Metas.Globally.Buttons.close = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/globally.close-mx.bmp"
                ), //
                Configuration.Offset.none(),
                0x000000
        );
        Metas.Globally.Dialogs.confirmQuitBattle = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/globally.confirm-quit-battle-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogConfirmQuitBattle(),
                0xFFFFFF
        );
        Metas.Globally.Dialogs.confirmStartNotFullTeam = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/globally.confirm-start-not-full-team-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogStartWithoutFullTeam(),
                0xFFFFFF
        );
        Metas.Globally.Dialogs.areYouStillThere = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/globally.are-you-still-there-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogAreYouStillThere(),
                0xFFFFFF
        );
        Metas.Dungeons.Buttons.rerun = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/dungeons.rerun-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonDungeonReRun(),
                0xFFFFFF);

        // Fishing
        Metas.Fishing.Labels.fishing = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "labels/fishing-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetLabelFishing(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.start = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/fishing.start-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingStart(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.cast = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/fishing.cast-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingCast(),
                0xFFFFFF
        );
        Metas.Fishing.Buttons.catch_ = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/fishing.catch-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFishingCatch(),
                0xFFFFFF
        );

        // World Boss
        Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.summon-party-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingPartiesWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnListingWorldBosses = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.summon-boss-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingWorldBosses(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.summon-tier-diff-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.startBoss = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.start-boss-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonStartWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroup = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.regroup-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonRegroupWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroupOnDefeated = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/world-boss.regroup-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonRegroupAfterDefeatByWorldBoss(),
                0xFFFFFF
        );
        Metas.WorldBoss.Dialogs.notEnoughXeals = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/world-boss.not-enough-xeals-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughXeals(),
                0xFFFFFF
        );

        // PVP Arena
        Metas.PvpArena.Buttons.play = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/pvp-arena.play-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonPlayPvpArena(),
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.fight1 = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/pvp-arena.fight-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFight1PvpArena(),
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.accept = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/pvp-arena.accept-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptPvpArena(),
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnWin = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/pvp-arena.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnWin(),
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnLose = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/pvp-arena.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnLose(),
                0xFFFFFF
        );
        Metas.PvpArena.Dialogs.notEnoughTicket = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/pvp-arena.not-enough-ticket-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughPvpTicket(),
                0xFFFFFF
        );

        // Invasion
        Metas.Invasion.Buttons.play = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/invasion.play-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonPlayInvasion(),
                0xFFFFFF
        );
        Metas.Invasion.Buttons.accept = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/invasion.accept-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptInvasion(),
                0xFFFFFF
        );
        Metas.Invasion.Buttons.town = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/invasion.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedInvasion(),
                0xFFFFFF
        );
        Metas.Invasion.Dialogs.notEnoughBadges = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/invasion.not-enough-badges-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughInvasionBadges(),
                0xFFFFFF
        );

        // Trials
        Metas.Trials.Buttons.play = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/trials.play-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonPlayTrials(),
                0xFFFFFF
        );
        Metas.Trials.Buttons.accept = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/trials.accept-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptTrials(),
                0xFFFFFF
        );
        Metas.Trials.Buttons.town = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/trials.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedTrials(),
                0xFFFFFF
        );
        Metas.Trials.Dialogs.notEnoughTokens = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "dialogs/trials.not-enough-tokens-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughTrialsTokens(),
                0xFFFFFF
        );

        // GVG
        Metas.Gvg.Buttons.play = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gvg.play-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonPlayGvg(),
                0xFFFFFF
        );
        Metas.Gvg.Buttons.fight = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gvg.fight-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonFight1Gvg(),
                0xFFFFFF
        );
        Metas.Gvg.Buttons.accept = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gvg.accept-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGvg(),
                0xFFFFFF
        );
        Metas.Gvg.Buttons.town = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gvg.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGvg(),
                0xFFFFFF
        );

        // Gauntlet
        Metas.Gauntlet.Buttons.play = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gauntlet.play-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonPlayGauntlet(),
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.accept = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gauntlet.accept-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGauntlet(),
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.town = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/gauntlet.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGauntlet(),
                0xFFFFFF
        );

        // Raid
        Metas.Raid.Buttons.town = new BwMatrixMeta(//
                ImageUtil.loadMxImageFromResource( //
                        "buttons/raid.town-mx.bmp"
                ), //
                Configuration.screenResolutionProfile.getOffsetButtonTownWhenDefeatedInRaid(),
                0xFFFFFF
        );
        Metas.Raid.Buttons.summon = BwMatrixMeta.fromTpImage(//
                "buttons/raid.summon-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetButtonSummonOfRaid(),
                0xFFFFFF
        );
        Metas.Raid.Labels.labelInSummonDialog = BwMatrixMeta.fromTpImage(//
                "labels/raid-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetLabelRaidInSummonDialog(),
                0xFFFFFF
        );

        // test
        testImgImportedFromTp();
    }

    private static void testImgImportedFromTp() throws IOException {
        if (Configuration.screenResolutionProfile instanceof ScreenResolutionProfile.WebProfile) {
            BwMatrixMeta g = BwMatrixMeta.fromTpImage(//
                    "buttons/globally.auto-green-tp.bmp", //
                    new Configuration.Offset(Configuration.screenResolutionProfile.getOffsetButtonAuto().X - 2, Configuration.screenResolutionProfile.getOffsetButtonAuto().Y - 2),
                    0xFFFFFF
            );
            BwMatrixMeta r = BwMatrixMeta.fromTpImage(//
                    "buttons/globally.auto-red-tp.bmp", //
                    new Configuration.Offset(Configuration.screenResolutionProfile.getOffsetButtonAuto().X - 2, Configuration.screenResolutionProfile.getOffsetButtonAuto().Y - 2),
                    0xFFFFFF
            );

            /*
            assert g.notAvailable == false;
            assert r.notAvailable == false;
            assert Metas.Globally.Buttons.autoG.notAvailable == false;
            assert Metas.Globally.Buttons.autoR.notAvailable == false;
            assert Metas.Globally.Buttons.autoG.coordinateOffset.X == g.coordinateOffset.X;
            assert Metas.Globally.Buttons.autoG.coordinateOffset.Y == g.coordinateOffset.Y;
            assert Metas.Globally.Buttons.autoR.coordinateOffset.X == r.coordinateOffset.X;
            assert Metas.Globally.Buttons.autoR.coordinateOffset.Y == r.coordinateOffset.Y;
            assert Metas.Globally.Buttons.autoG.w == g.w;
            assert Metas.Globally.Buttons.autoG.h == g.h;
            assert Metas.Globally.Buttons.autoR.w == r.w;
            assert Metas.Globally.Buttons.autoR.h == r.h;
             */
        }
    }

    public static BwMatrixMeta fromTpImage(String path, Configuration.Offset tpImageOffset, int blackPixelRgb) throws IOException {
        BufferedImageInfo bii = ImageUtil.loadTpImageFromResource(path);
        try {
            Tuple2<BufferedImageInfo, Configuration.Offset> transformed = ImageUtil.transformFromTpToMxImage(bii, blackPixelRgb, tpImageOffset);
            return new BwMatrixMeta(transformed._1, new Configuration.Offset(tpImageOffset.X + transformed._2.X, tpImageOffset.Y + transformed._2.Y), blackPixelRgb);
        } finally {
            ImageUtil.freeMem(bii.bufferedImage);
        }
    }
}