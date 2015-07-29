package ru.eclipsetrader.transaq.core.instruments;

import java.io.Closeable;
import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleStorage;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;


public class Instrument implements Closeable {
	
	public static int CANDLE_HISTORY_COUNT = 1000;
	private TQSymbol symbol;
	private QuoteGlass glass = new QuoteGlass();
	private CandleStorage candleStorage;
	IProcessingContext context;
	IDataFeedContext dataFeedContext;
	IAccount account;
	
	ListObserver<Quote> iQuotesObserver = new ListObserver<Quote>() {
		@Override
		public void update(List<Quote> quotes) {
			updateQuotes(quotes);
		}
	};
	
	ListObserver<Tick> iTickObserver = new ListObserver<Tick>() {
		@Override
		public void update(List<Tick> list) {
			//System.out.println("iTickObserver " + list.get(0).getSeccode() + " size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			for (Tick tick : list) {
				processTrade(tick);
			}
		}
	};

	public Instrument(TQSymbol symbol, IProcessingContext context) {
		this(symbol, context, TQInstrumentService.getInstance().getDefaultDataFeedContext());
	}
	
	public Instrument(TQSymbol symbol, IProcessingContext context, IDataFeedContext dataFeedContext) {
		this.symbol = symbol;
		this.context = context;
		this.dataFeedContext = dataFeedContext;
		this.candleStorage = new CandleStorage(this, context);
		this.account = dataFeedContext.getAccount(symbol);
		for (CandleType ct : context.getCandleTypes()) {
			CandleList candleList = candleStorage.createCandleTypeList(ct);
			candleList.appendCandles(dataFeedContext.getCandleList(symbol, ct, new Date(), CANDLE_HISTORY_COUNT));
		}
		dataFeedContext.getQuotesFeeder().addObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().addObserver(symbol, iTickObserver);
	}
	
	@Override
	public void close() throws java.io.IOException {
		dataFeedContext.getQuotesFeeder().deleteObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().deleteObserver(symbol, iTickObserver);
	};
	
	public TQSymbol getSymbol() {
		return symbol;
	}
	
	public QuoteGlass getQuoteGlass() {
		return glass;
	}
	
	public CandleStorage getCandleStorage() {
		return candleStorage;
	}
	
	public void updateQuotes(List<Quote> quotes) {
		glass.update(quotes);
		context.onQuotesChange(this, glass);
	}
		
	public void processTrade(Tick tick) {
		// добавим сделку в график свечей
		candleStorage.processTrade(tick);
		context.onTick(this, tick);
	}


}
