package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.internal.Tick;

public interface IDataFeedContext {

	void processTickTrade(Date tickTime, Tick tick);
	void processQuoteList(Date tickTime, List<Quote> quoteList);
	
}
