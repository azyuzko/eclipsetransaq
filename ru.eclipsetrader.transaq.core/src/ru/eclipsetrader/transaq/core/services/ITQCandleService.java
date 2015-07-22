package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface ITQCandleService extends IPersistable {

	List<CandleType> getCandleTypes();
	List<Candle> getHistoryData(ITQSymbol security, CandleType candleType, int count);
	
	void persist(TQSymbol symbol, CandleType candleType, List<Candle> candles);
	
}
