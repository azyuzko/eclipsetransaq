package ru.eclipsetrader.transaq.core.services;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface ITQCandleService  {

	List<CandleType> getCandleTypes();
	List<Candle> getHistoryData(TQSymbol symbol, CandleType candleType, int count);
	List<Candle> getSavedCandles(TQSymbol symbol, CandleType candleType, Date fromDate, Date toDate); 
	
}
