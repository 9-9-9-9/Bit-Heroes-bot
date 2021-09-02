package bh.bot.common.types.images;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.dev;
import static bh.bot.common.Log.err;
import static bh.bot.common.utils.ImageUtil.freeMem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.StringUtil;

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
    private final Short[][] originalTpPixelPart;

    public BwMatrixMeta(BufferedImageInfo mxbi, Configuration.Offset coordinateOffset, int blackPixelRgb, BufferedImage tpBi) {
        if (mxbi.notAvailable) {
            this.firstBlackPixelOffset = null;
            this.blackPixels = null;
            this.nonBlackPixels = null;
            this.w = 0;
            this.h = 0;
            this.blackPixelRgb = 0;
            this.blackPixelDRgb = null;
            this.coordinateOffset = null;
            this.tolerant = -1;
            this.imageNameCode = mxbi.code;
            this.notAvailable = true;
            this.originalTpPixelPart = null;
        } else {
            String customTolerantKey = "tolerant.color.bw|" + mxbi.code;
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
            BufferedImage img = mxbi.bufferedImage;
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

            this.imageNameCode = mxbi.code;
            this.notAvailable = false;
            
            if (tpBi == null) {
            	this.originalTpPixelPart = null;
            } else {
                this.originalTpPixelPart = new Short[tpBi.getWidth()][tpBi.getHeight()];
                for (int x = 0; x < tpBi.getWidth(); x++) {
					for (int y = 0; y < tpBi.getHeight(); y++) {
						Short value = null;
						final int rgb = tpBi.getRGB(x, y) & 0xFFFFFF;
						final int r = ImageUtil.getRed(rgb);
						final int g = ImageUtil.getGreen(rgb);
						final int b = ImageUtil.getBlue(rgb);
						if (r == g && g == b)
							value = (short)r;
						this.originalTpPixelPart[x][y] = value;
					}
				}
            }
        }
    }

    public boolean throwIfNotAvailable() {
        if (this.notAvailable) {
            err("Image is not available: %s, profile %s", this.imageNameCode, Configuration.profileName);
            if (!Configuration.noThrowWhenImageNotAvailable)
            	throw new NotSupportedException(String.format("Image is not available for profile '--%s': %s", Configuration.profileName, this.imageNameCode));
            return true;
        }
        return false;
    }

    public int[] getFirstBlackPixelOffset() {
        return firstBlackPixelOffset;
    }

    public boolean isMatchBlackRgb(int rgb) {
        return ImageUtil.areColorsSimilar(blackPixelDRgb, rgb, Configuration.Tolerant.color, getOriginalPixelPart(firstBlackPixelOffset[0], firstBlackPixelOffset[1]));
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
    
    public Short getOriginalPixelPart(int x, int y) {
    	if (this.originalTpPixelPart == null)
    		return null;
    	return this.originalTpPixelPart[x][y];
    }

    public static class Metas {
        public static class Globally {
            public static class Buttons {
                public static BwMatrixMeta talkRightArrow;
                public static BwMatrixMeta reconnect;
                public static BwMatrixMeta autoG;
                public static BwMatrixMeta autoR;
                public static BwMatrixMeta radioButton;
                public static BwMatrixMeta close;
                public static BwMatrixMeta mapButtonOnFamiliarUi;
            }

            public static class Dialogs {
                public static BwMatrixMeta confirmQuitBattle;
                public static BwMatrixMeta confirmStartNotFullTeam;
                public static BwMatrixMeta areYouStillThere;
                public static BwMatrixMeta areYouSureWantToExit;
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

            public static class Labels {
                public static BwMatrixMeta labelInSummonDialog;
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
                public static BwMatrixMeta accept;
            }

            public static class Labels {
                public static BwMatrixMeta labelInSummonDialog;
            }

            public static class Dialogs {
                public static BwMatrixMeta notEnoughShards;
            }
        }
    }

    public static void load() throws IOException {
        Metas.Globally.Buttons.talkRightArrow = BwMatrixMeta.from(//
                "buttons/globally.talkRightArrow?",
                Configuration.screenResolutionProfile.getOffsetButtonTalkRightArrow(), //
                0x000000
        );
        Metas.Globally.Buttons.reconnect = BwMatrixMeta.from(//
                "buttons/globally.reconnect?",
                Configuration.screenResolutionProfile.getOffsetButtonReconnect(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoG = BwMatrixMeta.from(//
                "buttons/globally.auto-green?",
                Configuration.screenResolutionProfile.getOffsetButtonAuto(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoR = BwMatrixMeta.from(//
                "buttons/globally.auto-red?",
                Configuration.screenResolutionProfile.getOffsetButtonAuto(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.radioButton = BwMatrixMeta.from(//
                "buttons/globally.radio-button?",
                Configuration.Offset.none(), //
                0x000000
        );
        Metas.Globally.Buttons.close = BwMatrixMeta.from(//
                "buttons/globally.close?",
                Configuration.Offset.none(), //
                0x000000
        );
        Metas.Globally.Buttons.mapButtonOnFamiliarUi = BwMatrixMeta.from(//
                "buttons/globally.map-on-familiar-ui?",
                Configuration.screenResolutionProfile.getOffsetButtonMapOnFamiliarUi(), //
                0x000000
        );
        Metas.Globally.Dialogs.confirmQuitBattle = BwMatrixMeta.from(//
                "dialogs/globally.confirm-quit-battle?",
                Configuration.screenResolutionProfile.getOffsetDialogConfirmQuitBattle(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.confirmStartNotFullTeam = BwMatrixMeta.from(//
                "dialogs/globally.confirm-start-not-full-team?",
                Configuration.screenResolutionProfile.getOffsetDialogStartWithoutFullTeam(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.areYouStillThere = BwMatrixMeta.from(//
                "dialogs/globally.are-you-still-there?",
                Configuration.screenResolutionProfile.getOffsetDialogAreYouStillThere(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.areYouSureWantToExit = BwMatrixMeta.from(//
                "dialogs/globally.are-you-sure-want-to-exit?",
                Configuration.screenResolutionProfile.getOffsetDialogAreYouSureWantToExit(), //
                0xFFFFFF
        );
        Metas.Dungeons.Buttons.rerun = BwMatrixMeta.from(//
                "buttons/dungeons.rerun?",
                Configuration.screenResolutionProfile.getOffsetButtonDungeonReRun(), //
                0xFFFFFF);

        // Fishing
        Metas.Fishing.Labels.fishing = BwMatrixMeta.from(//
                "labels/fishing?",
                Configuration.screenResolutionProfile.getOffsetLabelFishing(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.start = BwMatrixMeta.from(//
                "buttons/fishing.start?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingStart(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.cast = BwMatrixMeta.from(//
                "buttons/fishing.cast?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingCast(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.catch_ = BwMatrixMeta.from(//
                "buttons/fishing.catch?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingCatch(), //
                0xFFFFFF
        );

        // World Boss
        Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss = BwMatrixMeta.from(//
                "buttons/world-boss.summon-party?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingPartiesWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnListingWorldBosses = BwMatrixMeta.from(//
                "buttons/world-boss.summon-boss?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingWorldBosses(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty = BwMatrixMeta.from(//
                "buttons/world-boss.summon-tier-diff?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.startBoss = BwMatrixMeta.from(//
                "buttons/world-boss.start-boss?",
                Configuration.screenResolutionProfile.getOffsetButtonStartWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroup = BwMatrixMeta.from(//
                "buttons/world-boss.regroup-win?",
                Configuration.screenResolutionProfile.getOffsetButtonRegroupWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroupOnDefeated = BwMatrixMeta.from(//
                "buttons/world-boss.regroup-lose?",
                Configuration.screenResolutionProfile.getOffsetButtonRegroupAfterDefeatByWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Dialogs.notEnoughXeals = BwMatrixMeta.from(//
                "dialogs/world-boss.not-enough-xeals?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughXeals(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Labels.labelInSummonDialog = BwMatrixMeta.from(//
                "labels/world-boss-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetLabelWorldBossInSummonDialog(), //
                0xFFFFFF
        );

        // PVP Arena
        Metas.PvpArena.Buttons.play = BwMatrixMeta.from(//
                "buttons/pvp-arena.play?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayPvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.fight1 = BwMatrixMeta.from(//
                "buttons/pvp-arena.fight?",
                Configuration.screenResolutionProfile.getOffsetButtonFight1PvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.accept = BwMatrixMeta.from(//
                "buttons/pvp-arena.accept?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptPvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnWin = BwMatrixMeta.from(//
                "buttons/pvp-arena.town-win?",
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnWin(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnLose = BwMatrixMeta.from(//
                "buttons/pvp-arena.town-defeat?",
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnLose(), //
                0xFFFFFF
        );
        Metas.PvpArena.Dialogs.notEnoughTicket = BwMatrixMeta.from(//
                "dialogs/pvp-arena.not-enough-ticket?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughPvpTicket(), //
                0xFFFFFF
        );

        // Invasion
        Metas.Invasion.Buttons.play = BwMatrixMeta.from(//
                "buttons/invasion.play?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Buttons.accept = BwMatrixMeta.from(//
                "buttons/invasion.accept?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Buttons.town = BwMatrixMeta.from(//
                "buttons/invasion.town?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Dialogs.notEnoughBadges = BwMatrixMeta.from(//
                "dialogs/invasion.not-enough-badges?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughInvasionBadges(), //
                0xFFFFFF
        );

        // Trials
        Metas.Trials.Buttons.play = BwMatrixMeta.from(//
                "buttons/trials.play?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Buttons.accept = BwMatrixMeta.from(//
                "buttons/trials.accept?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Buttons.town = BwMatrixMeta.from(//
                "buttons/trials.town?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Dialogs.notEnoughTokens = BwMatrixMeta.from(//
                "dialogs/trials.not-enough-tokens?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughTrialsTokens(), //
                0xFFFFFF
        );

        // GVG
        Metas.Gvg.Buttons.play = BwMatrixMeta.from(//
                "buttons/gvg.play?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayGvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.fight = BwMatrixMeta.from(//
                "buttons/gvg.fight?",
                Configuration.screenResolutionProfile.getOffsetButtonFight1Gvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.accept = BwMatrixMeta.from(//
                "buttons/gvg.accept?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.town = BwMatrixMeta.from(//
                "buttons/gvg.town?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGvg(), //
                0xFFFFFF
        );

        // Gauntlet
        Metas.Gauntlet.Buttons.play = BwMatrixMeta.from(//
                "buttons/gauntlet.play?", //
                Configuration.screenResolutionProfile.getOffsetButtonPlayGauntlet(), //
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.accept = BwMatrixMeta.from(//
                "buttons/gauntlet.accept?", //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGauntlet(), //
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.town = BwMatrixMeta.from(//
                "buttons/gauntlet.town?", //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGauntlet(), //
                0xFFFFFF
        );

        // Raid
        Metas.Raid.Buttons.town = BwMatrixMeta.from(//
                "buttons/raid.town?", //
                Configuration.screenResolutionProfile.getOffsetButtonTownWhenDefeatedInRaid(), //
                0xFFFFFF
        );
        Metas.Raid.Buttons.accept = BwMatrixMeta.from(//
                "buttons/raid.accept-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptTeamOfRaid(), //
                0xFFFFFF
        );
        Metas.Raid.Labels.labelInSummonDialog = BwMatrixMeta.from(//
                "labels/raid-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetLabelRaidInSummonDialog(), //
                0xFFFFFF
        );
        Metas.Raid.Dialogs.notEnoughShards = BwMatrixMeta.from(//
                "dialogs/raid.not-enough-shards-tp.bmp", //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughShards(), //
                0xFFFFFF
        );
    }

    public static BwMatrixMeta from(String path, Configuration.Offset imageOffset, int blackPixelRgb) throws IOException {
        String normalized = path.trim().toLowerCase();
        if (normalized.endsWith("?")) {
            String prefix = path.substring(0, path.length() - 1);
            BwMatrixMeta bwMatrixMeta = new BwMatrixMeta(ImageUtil.loadMxImageFromResource(prefix + "-mx.bmp"), imageOffset, blackPixelRgb, null);
            if (bwMatrixMeta.notAvailable == false)
                return bwMatrixMeta;
            debug("MX type of %s is not available, going to load TP", path);
            if (bwMatrixMeta.notAvailable == false)
                debug("TP type of %s is not available either", path);
            else
            	debug("Loaded TP successfully for %s", path);
            bwMatrixMeta = fromTpImage(prefix + "-tp.bmp", imageOffset, blackPixelRgb);
            return bwMatrixMeta;
        } else if (normalized.endsWith("-mx.bmp"))
            return new BwMatrixMeta(ImageUtil.loadMxImageFromResource(path), imageOffset, blackPixelRgb, null);
        else if (normalized.endsWith("-tp.bmp"))
            return fromTpImage(path, imageOffset, blackPixelRgb);
        else
            throw new NotSupportedException("Un-recognized format");
    }

    public static BwMatrixMeta fromTpImage(String path, Configuration.Offset tpImageOffset, int blackPixelRgb) throws IOException {
        BufferedImageInfo tpbi = ImageUtil.loadTpImageFromResource(path);
        try {
            Tuple2<BufferedImageInfo, Configuration.Offset> transformed = ImageUtil.transformFromTpToMxImage(tpbi, blackPixelRgb, tpImageOffset);
            return new BwMatrixMeta(
        		transformed._1,
        		transformed._1.notAvailable
        		? null
        		: new Configuration.Offset(
        				tpImageOffset.X + transformed._2.X, 
        				tpImageOffset.Y + transformed._2.Y
        		), 
        		blackPixelRgb,
        		tpbi.bufferedImage
    		);
        } catch (Exception ex) {
        	err("Problem while loading tp image %s", path);
        	throw ex;
        } finally {
            ImageUtil.freeMem(tpbi.bufferedImage);
        }
    }
}