package bh.bot.common.types.images;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.dev;
import static bh.bot.common.Log.err;
import static bh.bot.common.utils.ImageUtil.freeMem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.StringUtil;

public class BwMatrixMeta {
    private final UUID id;
    private final int[] firstBlackPixelOffset;
    private final ArrayList<int[]> blackPixels;
    private final ArrayList<int[]> nonBlackPixels;
    private final int w;
    private final int h;
    private final int blackPixelRgb;
    private final ImageUtil.DynamicRgb blackPixelDRgb;
    private final Offset coordinateOffset;
    private final int[] lastMatch = new int[]{-1, -1};
    private final byte tolerant;
    private final String imageNameCode;
    public final boolean notAvailable;
    private final Short[][] originalTpPixelPart;

    public BwMatrixMeta(BufferedImageInfo mxBii, Offset coordinateOffset, int blackPixelRgb, BufferedImage tpBi) {
        this.id = UUID.randomUUID();
        if (mxBii.notAvailable) {
            this.firstBlackPixelOffset = null;
            this.blackPixels = null;
            this.nonBlackPixels = null;
            this.w = 0;
            this.h = 0;
            this.blackPixelRgb = 0;
            this.blackPixelDRgb = null;
            this.coordinateOffset = null;
            this.tolerant = -1;
            this.imageNameCode = mxBii.code;
            this.notAvailable = true;
            this.originalTpPixelPart = null;
        } else {
            String customTolerantKey = "tolerant.color.bw|" + mxBii.code;
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
            BufferedImage img = mxBii.bufferedImage;
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

            this.imageNameCode = mxBii.code;
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
        if (lastMatch[0] != x)
            lastMatch[0] = x;
        if (lastMatch[1] != y)
            lastMatch[1] = y;
    }

    public int[] getLastMatchPoint() {
        return lastMatch;
    }

    public int getTolerant() {
        return tolerant;
    }

    public Offset getCoordinateOffset() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BwMatrixMeta that = (BwMatrixMeta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Metas {
        public static class Globally {
            public static class Buttons {
                public static BwMatrixMeta talkRightArrow;
                public static BwMatrixMeta reconnect;
                public static BwMatrixMeta autoG;
                public static BwMatrixMeta autoR;
                public static BwMatrixMeta radioButton;
                public static BwMatrixMeta mapButtonOnFamiliarUi;
                public static BwMatrixMeta persuade;
                public static BwMatrixMeta persuadeBribe;
                public static BwMatrixMeta sendMessage;
            }

            public static class Dialogs {
                public static BwMatrixMeta confirmQuitBattle;
                public static BwMatrixMeta confirmStartNotFullTeam;
                public static BwMatrixMeta areYouStillThere;
                public static BwMatrixMeta areYouSureWantToExit;
                public static BwMatrixMeta news;
            }
        }

        public static class Persuade {
            public static class Labels {
            	public static BwMatrixMeta kaleido;
                public static BwMatrixMeta violace;
                public static BwMatrixMeta ragnar;
                public static BwMatrixMeta oevor;
                public static BwMatrixMeta grimz;
                public static BwMatrixMeta quirrel;
                public static BwMatrixMeta gobby;
                public static BwMatrixMeta rugumz;
                public static BwMatrixMeta moghur;
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
                public static BwMatrixMeta ready;
                public static BwMatrixMeta unready;
                public static BwMatrixMeta invite1;
                public static BwMatrixMeta invite2;
                public static BwMatrixMeta invite3;
                public static BwMatrixMeta invite4;
                public static BwMatrixMeta invite5;
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

        public static class Expedition {
            public static class Buttons {
                public static BwMatrixMeta play;
                public static BwMatrixMeta enter;
                public static BwMatrixMeta accept;
                public static BwMatrixMeta town;
            }

            public static class Labels {
                public static BwMatrixMeta infernoDimension;
                public static BwMatrixMeta hallowedDimension;
                public static BwMatrixMeta jammieDimension;
                public static BwMatrixMeta idolDimension;
                public static BwMatrixMeta battleBards;
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
                "buttons/globally.talkRightArrow2?",
                Configuration.screenResolutionProfile.getOffsetButtonTalkRightArrow(), //
                0x000000
        );
        Metas.Globally.Buttons.reconnect = BwMatrixMeta.from(//
                "buttons/globally.reconnect2?",
                Configuration.screenResolutionProfile.getOffsetButtonReconnect(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoG = BwMatrixMeta.from(//
                "buttons/globally.auto-green2?",
                Configuration.screenResolutionProfile.getOffsetButtonAuto(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.autoR = BwMatrixMeta.from(//
                "buttons/globally.auto-red2?",
                Configuration.screenResolutionProfile.getOffsetButtonAuto(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.radioButton = BwMatrixMeta.from(//
                "buttons/globally.radio-button2?",
                Offset.none(), //
                0x000000
        );
        Metas.Globally.Buttons.mapButtonOnFamiliarUi = BwMatrixMeta.from(//
                "buttons/globally.map-on-familiar-ui2?",
                Configuration.screenResolutionProfile.getOffsetButtonMapOnFamiliarUi(), //
                0x000000
        );
        Metas.Globally.Buttons.persuade = BwMatrixMeta.from(//
                "buttons/globally.persuade2?",
                Configuration.screenResolutionProfile.getOffsetButtonPersuade(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.persuadeBribe = BwMatrixMeta.from(//
                "buttons/globally.persuade-bribe2?",
                Configuration.screenResolutionProfile.getOffsetButtonBribePersuade(), //
                0xFFFFFF
        );
        Metas.Globally.Buttons.sendMessage = BwMatrixMeta.from(//
                "buttons/globally.send-message2?",
                Configuration.screenResolutionProfile.getOffsetButtonSendMessage(), //
                0xCEDEB5
        );
        Metas.Globally.Dialogs.confirmQuitBattle = BwMatrixMeta.from(//
                "dialogs/globally.confirm-quit-battle2?",
                Configuration.screenResolutionProfile.getOffsetDialogConfirmQuitBattle(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.confirmStartNotFullTeam = BwMatrixMeta.from(//
                "dialogs/globally.confirm-start-not-full-team2?",
                Configuration.screenResolutionProfile.getOffsetDialogStartWithoutFullTeam(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.areYouStillThere = BwMatrixMeta.from(//
                "dialogs/globally.are-you-still-there2?",
                Configuration.screenResolutionProfile.getOffsetDialogAreYouStillThere(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.areYouSureWantToExit = BwMatrixMeta.from(//
                "dialogs/globally.are-you-sure-want-to-exit2?",
                Configuration.screenResolutionProfile.getOffsetDialogAreYouSureWantToExit(), //
                0xFFFFFF
        );
        Metas.Globally.Dialogs.news = BwMatrixMeta.from(//
                "dialogs/globally.news2?",
                Configuration.screenResolutionProfile.getOffsetDialogNews(), //
                0xFFFFFF
        );
        Metas.Dungeons.Buttons.rerun = BwMatrixMeta.from(//
                "buttons/dungeons.rerun2?",
                Configuration.screenResolutionProfile.getOffsetButtonDungeonReRun(), //
                0xFFFFFF);
        
        // Persuade
        Metas.Persuade.Labels.kaleido = BwMatrixMeta.from(//
                "labels/persuade.kaleido?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeKaleido(), //
                0xFFFF00
        );
        Metas.Persuade.Labels.violace = BwMatrixMeta.from(//
                "labels/persuade.violace?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeViolace(), //
                0xFF807D
        );
        Metas.Persuade.Labels.ragnar = BwMatrixMeta.from(//
                "labels/persuade.ragnar?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeRagNar(), //
                0xFF807D
        );
        Metas.Persuade.Labels.oevor = BwMatrixMeta.from(//
                "labels/persuade.oevor?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeOevor(), //
                0xFF807D
        );
        Metas.Persuade.Labels.grimz = BwMatrixMeta.from(//
                "labels/persuade.grimz?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeGrimz(), //
                0xFF807D
        );
        Metas.Persuade.Labels.quirrel = BwMatrixMeta.from(//
                "labels/persuade.quirrel?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeQuirrel(), //
                0xFF807D
        );
        Metas.Persuade.Labels.gobby = BwMatrixMeta.from(//
                "labels/persuade.gobby?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeGobby(), //
                0xFFFF00
        );
        Metas.Persuade.Labels.rugumz = BwMatrixMeta.from(//
                "labels/persuade.rugumz?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeRugumz(), //
                0xFF807D
        );
        Metas.Persuade.Labels.moghur = BwMatrixMeta.from(//
                "labels/persuade.moghur?",
                Configuration.screenResolutionProfile.getOffsetLabelPersuadeMoghur(), //
                0xFF807D
        );
        
        // Fishing
        Metas.Fishing.Labels.fishing = BwMatrixMeta.from(//
                "labels/fishing.fishing2?",
                Configuration.screenResolutionProfile.getOffsetLabelFishing(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.start = BwMatrixMeta.from(//
                "buttons/fishing.start2?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingStart(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.cast = BwMatrixMeta.from(//
                "buttons/fishing.cast2?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingCast(), //
                0xFFFFFF
        );
        Metas.Fishing.Buttons.catch_ = BwMatrixMeta.from(//
                "buttons/fishing.catch2?",
                Configuration.screenResolutionProfile.getOffsetButtonFishingCatch(), //
                0xFFFFFF
        );

        // World Boss
        Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss = BwMatrixMeta.from(//
                "buttons/world-boss.summon-party2?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingPartiesWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnListingWorldBosses = BwMatrixMeta.from(//
                "buttons/world-boss.summon-boss2?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnListingWorldBosses(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty = BwMatrixMeta.from(//
                "buttons/world-boss.summon-tier-diff2?",
                Configuration.screenResolutionProfile.getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.startBoss = BwMatrixMeta.from(//
                "buttons/world-boss.start-boss2?",
                Configuration.screenResolutionProfile.getOffsetButtonStartWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroup = BwMatrixMeta.from(//
                "buttons/world-boss.regroup2?",
                Configuration.screenResolutionProfile.getOffsetButtonRegroupWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.regroupOnDefeated = BwMatrixMeta.from(//
                "buttons/world-boss.regroup2?",
                Configuration.screenResolutionProfile.getOffsetButtonRegroupAfterDefeatByWorldBoss(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.ready = BwMatrixMeta.from(//
                "buttons/world-boss.ready2?",
                Configuration.screenResolutionProfile.getOffsetButtonReadyWorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.unready = BwMatrixMeta.from(//
                "buttons/world-boss.unready2?",
                Configuration.screenResolutionProfile.getOffsetButtonUnReadyWorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.invite1 = BwMatrixMeta.from(//
                "buttons/world-boss.invite2?",
                Configuration.screenResolutionProfile.getOffsetButtonInvite1WorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.invite2 = BwMatrixMeta.from(//
                "buttons/world-boss.invite2?",
                Configuration.screenResolutionProfile.getOffsetButtonInvite2WorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.invite3 = BwMatrixMeta.from(//
                "buttons/world-boss.invite2?",
                Configuration.screenResolutionProfile.getOffsetButtonInvite3WorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.invite4 = BwMatrixMeta.from(//
                "buttons/world-boss.invite2?",
                Configuration.screenResolutionProfile.getOffsetButtonInvite4WorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Buttons.invite5 = BwMatrixMeta.from(//
                "buttons/world-boss.invite2?",
                Configuration.screenResolutionProfile.getOffsetButtonInvite5WorldBossTeam(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Dialogs.notEnoughXeals = BwMatrixMeta.from(//
                "dialogs/world-boss.not-enough-xeals2?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughXeals(), //
                0xFFFFFF
        );
        Metas.WorldBoss.Labels.labelInSummonDialog = BwMatrixMeta.from(//
                "labels/world-boss.world-boss2?", //
                Configuration.screenResolutionProfile.getOffsetLabelWorldBossInSummonDialog(), //
                0xFFFFFF
        );

        // PVP Arena
        Metas.PvpArena.Buttons.play = BwMatrixMeta.from(//
                "buttons/pvp-arena.play2?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayPvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.fight1 = BwMatrixMeta.from(//
                "buttons/pvp-arena.fight2?",
                Configuration.screenResolutionProfile.getOffsetButtonFight1PvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.accept = BwMatrixMeta.from(//
                "buttons/pvp-arena.accept2?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptPvpArena(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnWin = BwMatrixMeta.from(//
                "buttons/pvp-arena.town-win2?",
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnWin(), //
                0xFFFFFF
        );
        Metas.PvpArena.Buttons.townOnLose = BwMatrixMeta.from(//
                "buttons/pvp-arena.town-defeat2?",
                Configuration.screenResolutionProfile.getOffsetButtonBackToTownFromPvpArenaOnLose(), //
                0xFFFFFF
        );
        Metas.PvpArena.Dialogs.notEnoughTicket = BwMatrixMeta.from(//
                "dialogs/pvp-arena.not-enough-ticket2?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughPvpTicket(), //
                0xFFFFFF
        );

        // Invasion
        Metas.Invasion.Buttons.play = BwMatrixMeta.from(//
                "buttons/invasion.play2?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Buttons.accept = BwMatrixMeta.from(//
                "buttons/invasion.accept2?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Buttons.town = BwMatrixMeta.from(//
                "buttons/invasion.town2?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedInvasion(), //
                0xFFFFFF
        );
        Metas.Invasion.Dialogs.notEnoughBadges = BwMatrixMeta.from(//
                "dialogs/invasion.not-enough-badges2?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughInvasionBadges(), //
                0xFFFFFF
        );

        // Expedition
        Metas.Expedition.Buttons.play = BwMatrixMeta.from(//
                "buttons/expedition.play2?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayExpedition(), //
                0xFFFFFF
        );
        Metas.Expedition.Buttons.enter = BwMatrixMeta.from(//
                "buttons/expedition.enter?",
                Configuration.screenResolutionProfile.getOffsetButtonEnterExpedition(), //
                0xFFFFFF
        );
        Metas.Expedition.Buttons.accept = BwMatrixMeta.from(//
                "buttons/expedition.accept2?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptExpedition(), //
                0xFFFFFF
        );
        Metas.Expedition.Buttons.town = BwMatrixMeta.from(//
                "buttons/expedition.town2?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedExpedition(), //
                0xFFFFFF
        );
        Metas.Expedition.Labels.idolDimension = BwMatrixMeta.from(//
                "labels/expedition.idol-dimension?",
                Configuration.screenResolutionProfile.getOffsetLabelIdolDimension(), //
                0xFFFFFF
        );
        Metas.Expedition.Labels.hallowedDimension = BwMatrixMeta.from(//
                "labels/expedition.hallowed-dimension?",
                Configuration.screenResolutionProfile.getOffsetLabelHallowedDimension(), //
                0xFFFFFF
        );
        Metas.Expedition.Labels.infernoDimension = BwMatrixMeta.from(//
                "labels/expedition.inferno-dimension?",
                Configuration.screenResolutionProfile.getOffsetLabelInfernoDimension(), //
                0xFFFFFF
        );
        Metas.Expedition.Labels.battleBards = BwMatrixMeta.from(//
                "labels/expedition.battle-bards?",
                Configuration.screenResolutionProfile.getOffsetLabelBattleBards(), //
                0xFFFFFF
        );
        Metas.Expedition.Labels.jammieDimension = BwMatrixMeta.from(//
                "labels/expedition.jammie-dimension?",
                Configuration.screenResolutionProfile.getOffsetLabelJammieDimension(), //
                0xFFFFFF
        );

        // Trials
        Metas.Trials.Buttons.play = BwMatrixMeta.from(//
                "buttons/trials.play2?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Buttons.accept = BwMatrixMeta.from(//
                "buttons/trials.accept2?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Buttons.town = BwMatrixMeta.from(//
                "buttons/trials.town2?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedTrials(), //
                0xFFFFFF
        );
        Metas.Trials.Dialogs.notEnoughTokens = BwMatrixMeta.from(//
                "dialogs/trials.not-enough-tokens2?",
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughTrialsTokens(), //
                0xFFFFFF
        );

        // GVG
        Metas.Gvg.Buttons.play = BwMatrixMeta.from(//
                "buttons/gvg.play2?",
                Configuration.screenResolutionProfile.getOffsetButtonPlayGvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.fight = BwMatrixMeta.from(//
                "buttons/gvg.fight2?",
                Configuration.screenResolutionProfile.getOffsetButtonFight1Gvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.accept = BwMatrixMeta.from(//
                "buttons/gvg.accept2?",
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGvg(), //
                0xFFFFFF
        );
        Metas.Gvg.Buttons.town = BwMatrixMeta.from(//
                "buttons/gvg.town2?",
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGvg(), //
                0xFFFFFF
        );

        // Gauntlet
        Metas.Gauntlet.Buttons.play = BwMatrixMeta.from(//
                "buttons/gauntlet.play2?", //
                Configuration.screenResolutionProfile.getOffsetButtonPlayGauntlet(), //
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.accept = BwMatrixMeta.from(//
                "buttons/gauntlet.accept2?", //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptGauntlet(), //
                0xFFFFFF
        );
        Metas.Gauntlet.Buttons.town = BwMatrixMeta.from(//
                "buttons/gauntlet.town2?", //
                Configuration.screenResolutionProfile.getOffsetButtonTownAfterCompetedGauntlet(), //
                0xFFFFFF
        );

        // Raid
        Metas.Raid.Buttons.town = BwMatrixMeta.from(//
                "buttons/raid.town2?", //
                Configuration.screenResolutionProfile.getOffsetButtonTownWhenDefeatedInRaid(), //
                0xFFFFFF
        );
        Metas.Raid.Buttons.accept = BwMatrixMeta.from(//
                "buttons/raid.accept2?", //
                Configuration.screenResolutionProfile.getOffsetButtonAcceptTeamOfRaid(), //
                0xFFFFFF
        );
        Metas.Raid.Labels.labelInSummonDialog = BwMatrixMeta.from(//
                "labels/raid.raid2?", //
                Configuration.screenResolutionProfile.getOffsetLabelRaidInSummonDialog(), //
                0xFFFFFF
        );
        Metas.Raid.Dialogs.notEnoughShards = BwMatrixMeta.from(//
                "dialogs/raid.not-enough-shards2?", //
                Configuration.screenResolutionProfile.getOffsetDialogNotEnoughShards(), //
                0xFFFFFF
        );
    }

    public static BwMatrixMeta from(String path, Offset imageOffset, int blackPixelRgb) throws IOException {
        String normalized = path.trim().toLowerCase();
        if (normalized.endsWith("?")) {
            String prefix = path.substring(0, path.length() - 1);
            BwMatrixMeta bwMatrixMeta = new BwMatrixMeta(ImageUtil.loadMxImageFromResource(prefix + "-mx.bmp"), imageOffset, blackPixelRgb, null);
            if (!bwMatrixMeta.notAvailable)
                return bwMatrixMeta;
            debug("MX type of %s is not available, going to load TP", path);
            bwMatrixMeta = fromTpImage(prefix + "-tp.bmp", imageOffset, blackPixelRgb);
            if (bwMatrixMeta.notAvailable)
                debug("TP type of %s is not available either", path);
            else
                debug("Loaded TP successfully for %s", path);
            return bwMatrixMeta;
        } else if (normalized.endsWith("-mx.bmp"))
            return new BwMatrixMeta(ImageUtil.loadMxImageFromResource(path), imageOffset, blackPixelRgb, null);
        else if (normalized.endsWith("-tp.bmp"))
            return fromTpImage(path, imageOffset, blackPixelRgb);
        else
            throw new NotSupportedException("Un-recognized format");
    }

    public static BwMatrixMeta fromTpImage(String path, Offset tpImageOffset, int blackPixelRgb) throws IOException {
        BufferedImageInfo tpbi = ImageUtil.loadTpImageFromResource(path);
        try {
            Tuple2<BufferedImageInfo, Offset> transformed = ImageUtil.transformFromTpToMxImage(tpbi, blackPixelRgb, tpImageOffset);
            return new BwMatrixMeta(
        		transformed._1,
        		transformed._1.notAvailable
        		? null
        		: new Offset(
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