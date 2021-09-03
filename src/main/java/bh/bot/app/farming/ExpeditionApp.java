package bh.bot.app.farming;

import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import bh.bot.common.Configuration;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;

@AppCode(code = "expedition")
public class ExpeditionApp extends AbstractDoFarmingApp {
	private ExpeditionPlace place = ExpeditionPlace.Astamus;

	@Override
	protected boolean readMoreInput(BufferedReader br) throws IOException {
		info("1. %s", ExpeditionPlace.BlubLix);
		info("2. %s", ExpeditionPlace.Mowhi);
		info("3. %s", ExpeditionPlace.WizBot);
		info("4. %s", ExpeditionPlace.Astamus);
		place = readInput(br, "Select place to farm", "See above",
				new Function<String, Tuple3<Boolean, String, ExpeditionPlace>>() {
					@Override
					public Tuple3<Boolean, String, ExpeditionPlace> apply(String s) {
						try {
							int num = Integer.parseInt(s.trim());
							if (num == 1)
								return new Tuple3<>(true, null, ExpeditionPlace.BlubLix);
							if (num == 2)
								return new Tuple3<>(true, null, ExpeditionPlace.Mowhi);
							if (num == 3)
								return new Tuple3<>(true, null, ExpeditionPlace.WizBot);
							if (num == 4)
								return new Tuple3<>(true, null, ExpeditionPlace.Astamus);
							return new Tuple3<>(false, "Not a valid option", ExpeditionPlace.Astamus);
						} catch (NumberFormatException ex) {
							return new Tuple3<>(false, "Not a number", ExpeditionPlace.Astamus);
						}
					}
				});
		info("You have selected to farm %s", place.toString().toUpperCase());
		return true;
	}

	@Override
	protected String getAppShortName() {
		return "Expedition";
	}

	@Override
	protected AttendablePlace getAttendablePlace() {
		return AttendablePlaces.expedition;
	}

	@Override
	protected List<NextAction> getInternalPredefinedImageActions() {
		return ExpeditionApp.getPredefinedImageActions();
	}

	public static List<NextAction> getPredefinedImageActions() {
		return Arrays.asList(new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.play, false, false),
				new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.enter, false, false),
				new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.accept, false, false),
				new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.town, true, false),
				new NextAction(BwMatrixMeta.Metas.Invasion.Dialogs.notEnoughBadges, false, true));
	}

	@Override
	protected String getLimitationExplain() {
		return "This function does not support select badge cost, so choose it before turn this on";
	}

	@Override
	protected boolean doCustomAction() {
		if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.idolDimension)) {
			switch (this.place) {
			case BlubLix:
				moveCursor(Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionBlubLix()
						.toScreenCoordinate());
				mouseClick();
				return true;
			case Mowhi:
				moveCursor(
						Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionMowhi().toScreenCoordinate());
				mouseClick();
				return true;
			case WizBot:
				moveCursor(
						Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionWizBot().toScreenCoordinate());
				mouseClick();
				return true;
			case Astamus:
				moveCursor(Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionAstamus()
						.toScreenCoordinate());
				mouseClick();
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	public enum ExpeditionPlace {
		BlubLix, Mowhi, WizBot, Astamus
	}
}
