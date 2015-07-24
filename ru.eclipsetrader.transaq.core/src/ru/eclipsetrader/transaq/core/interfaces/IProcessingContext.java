package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;


public interface IProcessingContext {
	void completeTrade(TickTrade tick, Instrument i);
	void completeCandle(Candle candle);
	void complete(Instrument i);
	CandleType[] getCandleTypes();
}
