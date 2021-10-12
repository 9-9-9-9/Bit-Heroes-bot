package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.ReRunApp;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.app.farming.RaidApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Familiar;

public class FlagBribe extends FlagPattern<Familiar> {

	@Override
	protected Familiar internalParseParam(String paramPart) throws InvalidFlagException {
		String[] spl = paramPart.toLowerCase().split("[\\,\\;]");
		for (String s : spl) {
			switch (s) {
			case "kaleido":
				return Familiar.Kaleido;
			case "violace":
				return Familiar.Violace;
			case "ragnar":
				return Familiar.Ragnar;
			case "oevor":
				return Familiar.Oevor;
			case "grimz":
				return Familiar.Grimz;
			case "quirrel":
				return Familiar.Quirrel;
			case "gobby":
				return Familiar.Gobby;
			default:
				throw new NotSupportedException(String.format("Unknown value '%s' of flag %s", s, getCode()));
			}
		}
		throw new InvalidFlagException(String.format("Passing invalid value for flag %s", getCode()));
	}

	@Override
	public boolean isAllowParam() {
		return true;
	}

	@Override
	protected boolean isAllowMultiple() {
		return true;
	}

	@Override
	public boolean isGlobalFlag() {
		return false;
	}

	@Override
	public String getName() {
		return "bribe";
	}

	@Override
	public String getDescription() {
		return "Auto bribe with gems";
	}
	
	@Override
	protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
		return instance instanceof AfkApp
				|| instance instanceof ReRunApp
				|| instance instanceof RaidApp;
	}

	@Override
	public boolean hide() {
		return true;
	}
}
