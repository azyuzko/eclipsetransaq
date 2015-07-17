package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.interfaces.ICustomStorage;
import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.internal.Security;

public interface ITQSecurityService extends ICustomStorage<String, Security> {
	
	public Security getSecurity(ITQSymbol symbol);

	public List<Security> getMarketSecurities(MarketType market);
	
	public List<Security> getBoardSecurities(BoardType board);

	Security getSecurity(BoardType board, String seccode);
}
