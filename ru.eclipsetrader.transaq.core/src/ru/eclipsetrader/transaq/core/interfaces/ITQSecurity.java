package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.AssetType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.SecurityType;

public interface ITQSecurity extends ITQKey, ITQSymbol {

	public AssetType getAsset();
	public MarketType getMarket();
	public SecurityType getType();
	
}
