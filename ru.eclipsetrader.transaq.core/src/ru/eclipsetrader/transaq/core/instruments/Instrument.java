package ru.eclipsetrader.transaq.core.instruments;

import java.io.Closeable;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleStorage;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Utils;


public class Instrument implements Closeable {
	
	Logger logger = LogManager.getLogger("Instrument");
	
	public static int CANDLE_HISTORY_COUNT = 1000;
	private TQSymbol symbol;
	private QuoteGlass glass = new QuoteGlass();
	private CandleStorage candleStorage;
	private Quotation quotation;
	
	IProcessingContext context;
	IDataFeedContext dataFeedContext;

	ListObserver<Quote> iQuotesObserver = new ListObserver<Quote>() {
		@Override
		public void update(List<Quote> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iQuotesObserver " + list.get(0).getSeccode() + " size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			updateQuotes(list);
		}
	};
	
	ListObserver<Tick> iTickObserver = new ListObserver<Tick>() {
		@Override
		public void update(List<Tick> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iTickObserver " + list.get(0).getSeccode() + " size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			for (Tick tick : list) {
				processTrade(tick);
			}
		}
	};
	
	ListObserver<SymbolGapMap> iQuotationObserver = new ListObserver<SymbolGapMap>() {
		@Override
		public void update(List<SymbolGapMap> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iQuotationObserver " + list.get(0).getSeccode() + " size = " + list.size() + "  "+ Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			updateQuotations(list);
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
		for (CandleType ct : context.getCandleTypes()) {
			CandleList candleList = candleStorage.createCandleTypeList(ct);
			candleList.appendCandles(dataFeedContext.getCandleList(symbol, ct, new Date(), CANDLE_HISTORY_COUNT));
		}
		
		this.quotation =  new Quotation(symbol.getBoard(), symbol.getSeccode());
		
		dataFeedContext.getQuotesFeeder().addObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().addObserver(symbol, iTickObserver);
		dataFeedContext.getQuotationGapsFeeder().addObserver(symbol, iQuotationObserver);
	}
	
	@Override
	public void close() throws java.io.IOException {
		dataFeedContext.getQuotesFeeder().deleteObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().deleteObserver(symbol, iTickObserver);
	};
	
	/**
	 * �������� ���������� ���������� �������� �����������
	 */
	public void reset() {
		this.candleStorage = new CandleStorage(this, context);
		for (CandleType ct : context.getCandleTypes()) {
			CandleList candleList = candleStorage.createCandleTypeList(ct);
			candleList.appendCandles(dataFeedContext.getCandleList(symbol, ct, new Date(), CANDLE_HISTORY_COUNT));
		}		
		this.quotation =  new Quotation(symbol.getBoard(), symbol.getSeccode());		
	}
	
	public TQSymbol getSymbol() {
		return symbol;
	}
	
	public QuoteGlass getQuoteGlass() {
		return glass;
	}
	
	public CandleStorage getCandleStorage() {
		return candleStorage;
	}

	public Quotation getQuotation() {
		return quotation;
	}

	public void updateQuotes(List<Quote> quotes) {
		glass.update(quotes);
		context.onQuotesChange(this, glass);
	}
	
	public void updateQuotations(List<SymbolGapMap> list) {
		quotation.applyQuotationGap(list);
		context.onQuotationsChange(this, quotation);
	}
		
	public void processTrade(Tick tick) {
		// ������� ������ � ������ ������
		candleStorage.processTrade(tick);
		context.onTick(this, tick);
	}

	public QuantityCost buy(int quantity) {
		QuantityCost bought = dataFeedContext.getAccount().buy(symbol, quantity, glass);
		logger.info(Utils.formatDate(dataFeedContext.currentDate()) + " BUY: " + symbol + " requested = " + quantity + ", bought = " + bought + ", free = " + dataFeedContext.getAccount().getFree());
		return bought;
	}
	
	public QuantityCost sell(int quantity) {
		QuantityCost sold = dataFeedContext.getAccount().sell(symbol, quantity, glass);
		logger.info(Utils.formatDate(dataFeedContext.currentDate()) + " SELL: " + symbol + " requested = " + quantity + ", sold = " + sold + ", free = " + dataFeedContext.getAccount().getFree() );
		return sold;
	}

}
