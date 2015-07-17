package ru.eclipsetrader.transaq.core.services;

import ru.eclipsetrader.transaq.core.interfaces.ICustomStorage;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.internal.Market;

public interface ITQMarketService extends ICustomStorage<Integer, Market> {

	void put(Market market);
	
	MarketType getMarketType(Integer marketId);
	
}
