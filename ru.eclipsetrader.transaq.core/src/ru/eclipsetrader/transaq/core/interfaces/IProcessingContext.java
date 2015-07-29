package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.internal.Tick;

public interface IProcessingContext {
	void onTick(Instrument instrument, Tick tick);
	void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass);
	void onCandleClose(Instrument instrument, CandleList candleList, Candle closedCandle);
	void onCandleOpen(Instrument instrument, CandleList candleList, Candle openedCandle);
	void onCandleChange(Instrument instrument, CandleList candleList, Candle changedCandle);
	CandleType[] getCandleTypes();
	
}
