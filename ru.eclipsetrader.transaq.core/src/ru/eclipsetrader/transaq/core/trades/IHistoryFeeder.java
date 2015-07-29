package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IHistoryFeeder {
	
	List<Candle> getCandleList(TQSymbol symbol, CandleType candleType, Date fromDate, int count);
	
}
