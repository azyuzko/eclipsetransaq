package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;


public interface IStrategyContext {
	void candleChangeEvent(Instrument i, Candle candle);
	void candleCloseEvent(Instrument i, Candle candle);
	void tickTradeEvent(Instrument i, TickTrade tickTrade);
	void quotesUpdateEvent(Instrument i, QuoteGlass quoteGlass);
	void orderRequestedEvent(Instrument i, Order order);
}
