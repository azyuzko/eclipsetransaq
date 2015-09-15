package ru.eclipsetrader.transaq.core.feed;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;

public class TQDataFeed implements IDataFeedContext {
	
	static TQDataFeed instance;
	public static TQDataFeed getInstance() {
		if (instance == null) {
			instance = new TQDataFeed();
		}
		return instance;
	}

	InstrumentEvent<List<Tick>> tickListEvent = new InstrumentEvent<>("TQInstrumentService tick event");
	InstrumentEvent<List<Quote>> quoteListEvent = new InstrumentEvent<>("TQInstrumentService quote event");
	InstrumentEvent<List<SymbolGapMap>> quotationGapListEvent = new InstrumentEvent<>("TQInstrumentService quotation gap event");
		
	@Override
	public List<Candle> getCandleList(TQSymbol symbol, CandleType candleType,
			Date fromDate, int count) {
		return TQCandleService.getInstance().getHistoryData(symbol, candleType, count);
	}
	
	
	@Override
	public InstrumentEvent<List<Quote>> getQuotesFeeder() {
		return quoteListEvent;
	}
	
	@Override
	public InstrumentEvent<List<Tick>> getTicksFeeder() {
		return tickListEvent;
	}
	
	@Override
	public InstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder() {
		return quotationGapListEvent;
	}

	public void subscribeTicksFeed(TQSymbol symbol, Observer<List<Tick>> observer) {
		tickListEvent.addObserver(symbol, observer);
		TQTickTradeService.getInstance().subscribeAllTrades(symbol);
	}
	
	public void subscribeQuotesFeed(TQSymbol symbol, QuoteGlass quoteGlass) {
		quoteListEvent.addObserver(symbol, quoteGlass.getQuotesObserver());
		TQQuoteService.getInstance().subscribe(symbol);		
	}
	
	public void subscribeQuotationFeed(TQSymbol symbol, Quotation quotation) {
		quotationGapListEvent.addObserver(symbol, quotation.iQuotationObserver);
		TQQuotationService.getInstance().subscribe(symbol);
	}
	
	public void subscribeCandlesFeed(TQSymbol symbol, CandleList candleList) {
		tickListEvent.addObserver(symbol, candleList.getTickObserver());
		TQTickTradeService.getInstance().subscribeAllTrades(symbol);
	}
}
