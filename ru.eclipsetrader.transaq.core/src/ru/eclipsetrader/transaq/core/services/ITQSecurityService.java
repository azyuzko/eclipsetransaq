package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.interfaces.ICustomStorage;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Security;

public interface ITQSecurityService extends ICustomStorage<TQSymbol, Security> {
	
	public Security getSecurity(TQSymbol symbol);

	public List<Security> getMarketSecurities(MarketType market);
	
	public List<Security> getBoardSecurities(BoardType board);

}
