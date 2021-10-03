package bh.bot.common.types;

import java.awt.Rectangle;

import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.tuples.Tuple4;

@SuppressWarnings("SpellCheckingInspection")
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

	public abstract Tuple4<Integer, Integer, Integer, Integer> getBackwardScanRightSideAttendablePlaces(); // min x, max
																											// y, step
																											// backward,
																											// max x

	public abstract Tuple4<Integer, Integer, Integer, Integer> getBackwardScanLeftSideAttendablePlaces(); // min x, min
																											// y, step
																											// forward,
																											// max x

	public abstract Offset getOffsetButtonSummonOnListingPartiesWorldBoss();

	public abstract Offset getOffsetLabelWorldBossInSummonDialog();

	public abstract Offset getOffsetButtonSummonOnListingWorldBosses();

	public abstract Offset getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty();

	public abstract Offset getOffsetButtonStartWorldBoss();

	public abstract Offset getOffsetDialogConfirmQuitBattle();

	public abstract Offset getOffsetDialogStartWithoutFullTeam();

	public abstract Offset getOffsetDialogAreYouStillThere();

	public abstract Offset getOffsetButtonRegroupWorldBoss();

	public abstract Offset getOffsetButtonRegroupAfterDefeatByWorldBoss();

	public abstract Offset getOffsetDialogNotEnoughXeals();

	public abstract Offset getOffsetButtonPlayPvpArena();

	public abstract Offset getOffsetButtonFight1PvpArena();

	public abstract Offset getOffsetButtonAcceptPvpArena();

	public abstract Offset getOffsetButtonBackToTownFromPvpArenaOnWin();

	public abstract Offset getOffsetButtonBackToTownFromPvpArenaOnLose();

	public abstract Offset getOffsetDialogNotEnoughPvpTicket();

	public abstract Offset getOffsetButtonPlayInvasion();

	public abstract Offset getOffsetButtonAcceptInvasion();

	public abstract Offset getOffsetButtonAcceptExpedition();

	public abstract Offset getOffsetButtonTownAfterCompetedInvasion();

	public abstract Offset getOffsetButtonPlayExpedition();

	public abstract Offset getOffsetButtonEnterExpedition();

	public abstract Offset getOffsetButtonTownAfterCompetedExpedition();

	public abstract Offset getOffsetLabelIdolDimension();

	public abstract Offset getOffsetEnterIdolDimensionBlubLix();

	public abstract Offset getOffsetEnterIdolDimensionMowhi();

	public abstract Offset getOffsetEnterIdolDimensionWizBot();

	public abstract Offset getOffsetEnterIdolDimensionAstamus();

	public abstract Offset getOffsetLabelInfernoDimension();

	public abstract Offset getOffsetEnterInfernoDimensionRaleib();

	public abstract Offset getOffsetEnterInfernoDimensionBlemo();

	public abstract Offset getOffsetEnterInfernoDimensionGummy();

	public abstract Offset getOffsetEnterInfernoDimensionZarlock();

	public abstract Offset getOffsetLabelHallowedDimension();

	public abstract Offset getOffsetEnterHallowedDimensionGooGarum();

	public abstract Offset getOffsetEnterHallowedDimensionSvord();

	public abstract Offset getOffsetEnterHallowedDimensionTwimbo();

	public abstract Offset getOffsetEnterHallowedDimensionX5T34M();

	public abstract Offset getOffsetLabelJammieDimension();

	public abstract Offset getOffsetEnterJammieDimensionZorgo();

	public abstract Offset getOffsetEnterJammieDimensionYackerz();

	public abstract Offset getOffsetEnterJammieDimensionVionot();

	public abstract Offset getOffsetEnterJammieDimensionGrampa();

	public abstract Offset getOffsetLabelBattleBards();

	public abstract Offset getOffsetEnterBattleBardsHero();

	public abstract Offset getOffsetEnterBattleBardsBurning();

	public abstract Offset getOffsetEnterBattleBardsMelvapaloozo();

	public abstract Offset getOffsetEnterBattleBardsBitstock();

	public abstract Offset getOffsetDialogNotEnoughInvasionBadges();

	public abstract Offset getOffsetButtonPlayTrials();

	public abstract Offset getOffsetButtonAcceptTrials();

	public abstract Offset getOffsetButtonTownAfterCompetedTrials();

	public abstract Offset getOffsetDialogNotEnoughTrialsTokens();

	public abstract Offset getOffsetButtonPlayGvg();

	public abstract Offset getOffsetButtonFight1Gvg();

	public abstract Offset getOffsetButtonAcceptGvg();

	public abstract Offset getOffsetButtonTownAfterCompetedGvg();

	public abstract Offset getOffsetButtonPlayGauntlet();

	public abstract Offset getOffsetButtonAcceptGauntlet();

	public abstract Offset getOffsetButtonTownAfterCompetedGauntlet();

	public abstract Offset getOffsetButtonAuto();

	public abstract Offset getOffsetButtonTownWhenDefeatedInRaid();

	public abstract Rectangle getRectangleRadioButtonsOfRaid();

	public abstract Rectangle getRectangleRadioButtonsOfWorldBoss();

	public abstract Offset getOffsetLabelRaidInSummonDialog();

	public abstract Offset getOffsetButtonSummonOfRaid();

	public abstract Offset getOffsetButtonAcceptTeamOfRaid();

	public abstract Offset getOffsetDialogNotEnoughShards();

	public abstract Offset getOffsetButtonEnterNormalRaid();

	public abstract Offset getOffsetButtonEnterHardRaid();

	public abstract Offset getOffsetButtonEnterHeroicRaid();

	public abstract Offset getOffsetButtonMapOnFamiliarUi();

	public abstract Offset getOffsetDialogAreYouSureWantToExit();

	public abstract Offset getOffsetDialogNews();

	public abstract Offset getOffsetButtonPersuade();
	
	public abstract Offset getOffsetButtonBribePersuade();
	
	public abstract Offset getOffsetLabelPersuadeKaleido();

	public static class Profile800x520 extends ScreenResolutionProfile {
		public static final String profileName = "800x520";

		@Override
		public String getName() {
			return profileName;
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
		public Offset getOffsetLabelWorldBossInSummonDialog() {
			return new Offset(295, 104);
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
		public Offset getOffsetDialogConfirmQuitBattle() {
			return new Offset(288, 232);
		}

		@Override
		public Offset getOffsetDialogStartWithoutFullTeam() {
			return new Offset(279, 221);
		}

		@Override
		public Offset getOffsetDialogAreYouStillThere() {
			return new Offset(314, 242);
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

		@Override
		public Offset getOffsetButtonPlayInvasion() {
			return new Offset(520, 270);
		}

		@Override
		public Offset getOffsetButtonAcceptInvasion() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedInvasion() {
			return new Offset(387, 468);
		}

		@Override
		public Offset getOffsetDialogNotEnoughInvasionBadges() {
			return new Offset(279, 231);
		}

		@Override
		public Offset getOffsetButtonPlayTrials() {
			return new Offset(517, 271);
		}

		@Override
		public Offset getOffsetButtonAcceptTrials() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedTrials() {
			return new Offset(387, 468);
		}

		@Override
		public Offset getOffsetDialogNotEnoughTrialsTokens() {
			return new Offset(279, 231);
		}

		@Override
		public Offset getOffsetButtonPlayGvg() {
			return new Offset(521, 272);
		}

		@Override
		public Offset getOffsetButtonFight1Gvg() {
			return new Offset(575, 198);
		}

		@Override
		public Offset getOffsetButtonAcceptGvg() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedGvg() {
			return new Offset(387, 468);
		}

		@Override
		public Offset getOffsetButtonPlayGauntlet() {
			return new Offset(520, 271);
		}

		@Override
		public Offset getOffsetButtonAcceptGauntlet() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedGauntlet() {
			return new Offset(387, 468);
		}

		@Override
		public Offset getOffsetButtonAuto() {
			return new Offset(778, 213);
		}

		@Override
		public Offset getOffsetButtonTownWhenDefeatedInRaid() {
			return new Offset(387, 468);
		}

		@Override
		public Rectangle getRectangleRadioButtonsOfRaid() {
			return new Rectangle(163, 435, 474, 15);
		}

		@Override
		public Rectangle getRectangleRadioButtonsOfWorldBoss() {
			return new Rectangle(163, 435, 474, 15);
		}

		@Override
		public Offset getOffsetLabelRaidInSummonDialog() {
			return new Offset(365, 104);
		}

		@Override
		public Offset getOffsetButtonSummonOfRaid() {
			return new Offset(490, 365);
		}

		@Override
		public Offset getOffsetButtonAcceptTeamOfRaid() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetDialogNotEnoughShards() {
			return new Offset(279, 231);
		}

		@Override
		public Offset getOffsetButtonEnterNormalRaid() {
			return new Offset(206, 233);
		}

		@Override
		public Offset getOffsetButtonEnterHardRaid() {
			return new Offset(398, 233);
		}

		@Override
		public Offset getOffsetButtonEnterHeroicRaid() {
			return new Offset(590, 233);
		}

		@Override
		public Offset getOffsetButtonMapOnFamiliarUi() {
			return new Offset(66, 135);
		}

		@Override
		public Offset getOffsetDialogAreYouSureWantToExit() {
			return new Offset(289, 231);
		}

		@Override
		public Offset getOffsetButtonPlayExpedition() {
			return new Offset(521, 269);
		}

		@Override
		public Offset getOffsetButtonEnterExpedition() {
			return new Offset(362, 452);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedExpedition() {
			return new Offset(387, 468);
		}

		@Override
		public Offset getOffsetLabelIdolDimension() {
			return new Offset(242, 57);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionBlubLix() {
			return new Offset(400, 183);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionMowhi() {
			return new Offset(242, 389);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionWizBot() {
			return new Offset(559, 389);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionAstamus() {
			return new Offset(400, 311);
		}

		@Override
		public Offset getOffsetLabelInfernoDimension() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionRaleib() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionBlemo() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionGummy() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionZarlock() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetLabelHallowedDimension() {
			return new Offset(208, 58);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionGooGarum() {
			return new Offset(205, 180);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionSvord() {
			return new Offset(511, 208);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionTwimbo() {
			return new Offset(354, 341);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionX5T34M() {
			return new Offset(649, 378);
		}

		@Override
		public Offset getOffsetLabelJammieDimension() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionZorgo() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionYackerz() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionVionot() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionGrampa() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetLabelBattleBards() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsHero() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsBurning() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsMelvapaloozo() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsBitstock() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetButtonAcceptExpedition() {
			return new Offset(474, 457);
		}

		@Override
		public Offset getOffsetDialogNews() {
			return new Offset(356, 77);
		}

		@Override
		public Offset getOffsetButtonPersuade() {
			return new Offset(139, 329);
		}

		@Override
		public Offset getOffsetButtonBribePersuade() {
			return new Offset(567, 329);
		}

		@Override
		public Offset getOffsetLabelPersuadeKaleido() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Deprecated
	public static class WebProfile extends Profile800x520 {
		private WebProfile() {
			throw new NotSupportedException("Deprecated! This class can not be initialized");
		}

		@Override
		public String getName() {
			return "web";
		}
	}

	@Deprecated
	public static class SteamProfile extends ScreenResolutionProfile {
		private SteamProfile() {
			throw new NotSupportedException("Deprecated! This class can not be initialized");
		}

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
			return new Offset(358, 12); //
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
			return new Offset(572, 326); //
		}

		@Override
		public Size getScanSizeCastingFish() {
			return new Size(45, 68); //
		}

		@Override
		public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanRightSideAttendablePlaces() {
			return new Tuple4<>(730, 397, -67, 793);
		}

		@Override
		public Tuple4<Integer, Integer, Integer, Integer> getBackwardScanLeftSideAttendablePlaces() {
			return new Tuple4<>(11, 61, 67, 56);
		}

		@Override
		public Offset getOffsetButtonSummonOnListingPartiesWorldBoss() {
			return new Offset(449, 419);
		}

		@Override
		public Offset getOffsetLabelWorldBossInSummonDialog() {
			return new Offset(301, 94);
		}

		@Override
		public Offset getOffsetButtonSummonOnListingWorldBosses() {
			return new Offset(490, 342);
		}

		@Override
		public Offset getOffsetButtonSummonOnSelectingWorldBossTierAndDifficulty() {
			return new Offset(430, 366);
		}

		@Override
		public Offset getOffsetButtonStartWorldBoss() {
			return new Offset(346, 428);
		}

		@Override
		public Offset getOffsetDialogConfirmQuitBattle() {
			return new Offset(296, 213);
		}

		@Override
		public Offset getOffsetDialogStartWithoutFullTeam() {
			return new Offset(287, 204);
		}

		@Override
		public Offset getOffsetDialogAreYouStillThere() {
			return new Offset(326, 253);
		}

		@Override
		public Offset getOffsetButtonRegroupWorldBoss() {
			return new Offset(370, 435);
		}

		@Override
		public Offset getOffsetButtonRegroupAfterDefeatByWorldBoss() {
			return new Offset(299, 435);
		}

		@Override
		public Offset getOffsetDialogNotEnoughXeals() {
			return new Offset(292, 213);
		}

		@Override
		public Offset getOffsetButtonPlayPvpArena() {
			return new Offset(512, 246);
		}

		@Override
		public Offset getOffsetButtonFight1PvpArena() {
			return new Offset(564, 182);
		}

		@Override
		public Offset getOffsetButtonAcceptPvpArena() {
			return new Offset(469, 424);
		}

		@Override
		public Offset getOffsetButtonBackToTownFromPvpArenaOnWin() {
			return new Offset(458, 435);
		}

		@Override
		public Offset getOffsetButtonBackToTownFromPvpArenaOnLose() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetDialogNotEnoughPvpTicket() {
			return new Offset(284, 213);
		}

		@Override
		public Offset getOffsetButtonPlayInvasion() {
			return new Offset(513, 250);
		}

		@Override
		public Offset getOffsetButtonAcceptInvasion() {
			return new Offset(470, 424);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedInvasion() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetDialogNotEnoughInvasionBadges() {
			return new Offset(286, 213);
		}

		@Override
		public Offset getOffsetButtonPlayTrials() {
			return new Offset(510, 250);
		}

		@Override
		public Offset getOffsetButtonAcceptTrials() {
			return new Offset(470, 424);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedTrials() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetDialogNotEnoughTrialsTokens() {
			return new Offset(286, 213);
		}

		@Override
		public Offset getOffsetButtonPlayGvg() {
			return new Offset(514, 251);
		}

		@Override
		public Offset getOffsetButtonFight1Gvg() {
			return new Offset(564, 182);
		}

		@Override
		public Offset getOffsetButtonAcceptGvg() {
			return new Offset(470, 424);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedGvg() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetButtonPlayGauntlet() {
			return new Offset(513, 250);
		}

		@Override
		public Offset getOffsetButtonAcceptGauntlet() {
			return new Offset(470, 424);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedGauntlet() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetButtonAuto() {
			return new Offset(779, 196); //
		}

		@Override
		public Offset getOffsetButtonTownWhenDefeatedInRaid() {
			return new Offset(388, 435); //
		}

		@Override
		public Rectangle getRectangleRadioButtonsOfRaid() {
			return new Rectangle(179, 403, 423, 13);
		}

		@Override
		public Rectangle getRectangleRadioButtonsOfWorldBoss() {
			return new Rectangle(197, 404, 406, 13);
		}

		@Override
		public Offset getOffsetLabelRaidInSummonDialog() {
			return new Offset(367, 94);
		}

		@Override
		public Offset getOffsetButtonSummonOfRaid() {
			return new Offset(486, 340);
		}

		@Override
		public Offset getOffsetButtonAcceptTeamOfRaid() {
			return new Offset(470, 424);
		}

		@Override
		public Offset getOffsetDialogNotEnoughShards() {
			return new Offset(286, 213);
		}

		@Override
		public Offset getOffsetButtonEnterNormalRaid() {
			return new Offset(218, 215);
		}

		@Override
		public Offset getOffsetButtonEnterHardRaid() {
			return new Offset(399, 215);
		}

		@Override
		public Offset getOffsetButtonEnterHeroicRaid() {
			return new Offset(579, 215);
		}

		@Override
		public Offset getOffsetButtonMapOnFamiliarUi() {
			return new Offset(86, 123);
		}

		@Override
		public Offset getOffsetDialogAreYouSureWantToExit() {
			return new Offset(296, 213);
		}

		@Override
		public Offset getOffsetButtonPlayExpedition() {
			return new Offset(514, 249);
		}

		@Override
		public Offset getOffsetButtonEnterExpedition() {
			return new Offset(364, 420);
		}

		@Override
		public Offset getOffsetButtonTownAfterCompetedExpedition() {
			return new Offset(388, 435);
		}

		@Override
		public Offset getOffsetLabelIdolDimension() {
			return new Offset(251, 49);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionBlubLix() {
			return new Offset(400, 169);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionMowhi() {
			return new Offset(250, 363);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionWizBot() {
			return new Offset(551, 363);
		}

		@Override
		public Offset getOffsetEnterIdolDimensionAstamus() {
			return new Offset(400, 286);
		}

		@Override
		public Offset getOffsetLabelInfernoDimension() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionRaleib() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionBlemo() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionGummy() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterInfernoDimensionZarlock() {
			throw new NotImplementedException("Inferno Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetLabelHallowedDimension() {
			return new Offset(220, 50);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionGooGarum() {
			return new Offset(216, 167);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionSvord() {
			return new Offset(504, 193);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionTwimbo() {
			return new Offset(357, 318);
		}

		@Override
		public Offset getOffsetEnterHallowedDimensionX5T34M() {
			return new Offset(634, 349);
		}

		@Override
		public Offset getOffsetLabelJammieDimension() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionZorgo() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionYackerz() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionVionot() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterJammieDimensionGrampa() {
			throw new NotImplementedException("Jammie Dimension of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetLabelBattleBards() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsHero() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsBurning() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsMelvapaloozo() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetEnterBattleBardsBitstock() {
			throw new NotImplementedException("Battle Bards of Expedition has not yet been implemented");
		}

		@Override
		public Offset getOffsetButtonAcceptExpedition() {
			return new Offset(469, 424);
		}

		@Override
		public Offset getOffsetDialogNews() {
			return new Offset(359, 69);
		}

		@Override
		public Offset getOffsetButtonPersuade() {
			return new Offset(155, 305);
		}

		@Override
		public Offset getOffsetButtonBribePersuade() {
			return new Offset(556, 305);
		}

		@Override
		public Offset getOffsetLabelPersuadeKaleido() {
			return new Offset(202, 70);
		}
	}
}
