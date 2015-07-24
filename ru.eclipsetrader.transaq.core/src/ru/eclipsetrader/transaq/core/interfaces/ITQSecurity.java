package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.AssetType;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.SecurityType;

public interface ITQSecurity extends ITQKey {
	public String getSeccode();
	public AssetType getAsset();
	public MarketType getMarket();
	public SecurityType getType();
	public BoardType getBoard();
}
