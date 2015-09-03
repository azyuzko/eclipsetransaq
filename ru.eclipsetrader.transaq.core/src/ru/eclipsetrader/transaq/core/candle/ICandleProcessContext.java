package ru.eclipsetrader.transaq.core.candle;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface ICandleProcessContext {
	
	void onCandleClose(TQSymbol symbol, CandleList candleList, Candle closedCandle);

}
