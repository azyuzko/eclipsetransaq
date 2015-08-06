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
	
	final IProcessingContext context;
	final IDataFeedContext dataFeedContext;

	public IProcessingContext getContext() {
		return context;
	}

	ListObserver<Quote> iQuotesObserver = new ListObserver<Quote>() {
		@Override
		public void update(List<Quote> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iQuotesObserver " + list.get(0).getSeccode() + " Context = " + context + "  size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			updateQuotes(list);
		}
	};
	
	ListObserver<Tick> iTickObserver = new ListObserver<Tick>() {
		@Override
		public void update(List<Tick> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iTickObserver " + list.get(0).getSeccode()+ " Context = " + context + "  size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
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
				logger.debug("iQuotationObserver " + list.get(0).getSeccode() + " Context = " + context + "  size = " + list.size() + "  "+ Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			updateQuotations(list);
		}
	};

	public Instrument(TQSymbol symbol, IProcessingContext context) {
		this(symbol, context, TQInstrumentService.getInstance().getDefaultDataFeedContext());
	}
	
	public Instrument(TQSymbol symbol, IProcessingContext context, IDataFeedContext dataFeedContext) {
		if (logger.isDebugEnabled()) {
			// logger.debug("Creating new instrument for " + symbol + ", ProcessingContext = " + context);
		}
		this.symbol = symbol;
		this.context = context;
		this.dataFeedContext = dataFeedContext;
		this.candleStorage = new CandleStorage(this, context);
		for (CandleType ct : context.getCandleTypes()) {
			CandleList candleList = this.candleStorage.getCandleList(ct);
			candleList.appendCandles(dataFeedContext.getCandleList(symbol, ct, new Date(), CANDLE_HISTORY_COUNT));
		}		
		this.quotation =  new Quotation(symbol.getBoard(), symbol.getSeccode());
		this.glass = new QuoteGlass();
	}
	
	@Override
	public void close() throws java.io.IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Closing instrument " + symbol + ", ProcessingContext = " + context);
		}
		dataFeedContext.getQuotesFeeder().deleteObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().deleteObserver(symbol, iTickObserver);
	};
	
	/**
	 * Обнуляет содержимое внутренних объектов инструмента
	 */
	public void init() {
		logger.debug("Reset instrument settings " + symbol + " " + Integer.toHexString(hashCode()) + " context " + Integer.toHexString(context.hashCode()));
		dataFeedContext.getQuotesFeeder().addObserver(symbol, iQuotesObserver);
		dataFeedContext.getTicksFeeder().addObserver(symbol, iTickObserver);
		dataFeedContext.getQuotationGapsFeeder().addObserver(symbol, iQuotationObserver);
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
		logger.debug("Update quotes " + symbol);
		glass.update(quotes);
		context.onQuotesChange(this, glass);
	}
	
	public void updateQuotations(List<SymbolGapMap> list) {
		quotation.applyQuotationGap(list);
		context.onQuotationsChange(this, quotation);
	}
		
	public void processTrade(Tick tick) {
		// добавим сделку в график свечей
		candleStorage.processTrade(tick);
		context.onTick(this, tick);
	}

	public QuantityCost buy(int quantity) {
		if (glass.getSellStack().size() == 0) {
			logger.warn("Trying to buy on empty quote glass!");
			return new QuantityCost(0,0);
		} else {
			QuantityCost bought = context.getAccount().buy(symbol, quantity, glass);
			logger.info(Utils.formatDate(context.getDateTime()) + " BUY: " + symbol + " requested = " + quantity + ", bought = " + bought + ", free = " + context.getAccount().getFree());
			return bought;
		}
	}
	
	public QuantityCost sell(int quantity) {
		if (glass.getBuyStack().size() == 0) {
			logger.warn(Utils.formatTime(context.getDateTime()) + " Trying to sell on empty quote glass!");
			return new QuantityCost(0,0);
		} else {
			QuantityCost sold = context.getAccount().sell(symbol, quantity, glass);
			logger.info(Utils.formatDate(context.getDateTime()) + " SELL: " + symbol + " requested = " + quantity + ", sold = " + sold + ", free = " + context.getAccount().getFree() );
			return sold;
		}
	}

}
