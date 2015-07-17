package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;


public interface ITQTickTradeService {

	void subscribeAllTrades(TQSymbol symbol);
	void unsubscribeAllTrades(TQSymbol symbol);
	List<Tick> getCurrentDayTickData(TQSymbol symbol);
	void subscribeTicks(TQSymbol symbol);
	void unsubscribeTicks(TQSymbol symbol);
	
}
