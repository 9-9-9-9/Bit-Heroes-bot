package bh.bot.app.farming;

import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;

import java.util.Arrays;
import java.util.List;

import bh.bot.common.Configuration;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;

@AppCode(code = "expedition")
public class ExpeditionApp extends AbstractDoFarmingApp {
	private final ExpeditionPlace place = ExpeditionPlace.Astamus;

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
