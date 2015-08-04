package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.SimpleAccount;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.IInstrumentEvent;
import ru.eclipsetrader.transaq.core.event.SynchronousInstrumentEvent;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 *  ормилец данными из Ѕƒ
 * @author Zyuzko-AA
 *
 */
public class DataFeeder implements IDataFeedContext {
	
	Logger logger = LogManager.getLogger("DataFeeder");
	
	Date fromDate;
	Date toDate;
	
	ThreadLocal<Long> threadTickTime = new ThreadLocal<Long>();

	static int TICK_PERIOD = 1; // период тика в милисекундах
	
	SynchronousInstrumentEvent<List<Tick>> ticksEvent = new SynchronousInstrumentEvent<>("Tick DB emulator");
	SynchronousInstrumentEvent<List<Quote>> quotesEvent = new SynchronousInstrumentEvent<>("Quotes DB emulator");
	SynchronousInstrumentEvent<List<SymbolGapMap>> quotationEvent = new SynchronousInstrumentEvent<>("Quotation DB emulator");
	
	Boolean feedPrepared = false;
	
	TreeMap<Long, List<TickTrade>> tickFeed;
	TreeMap<Long, List<Quote>> quoteFeed;
	TreeMap<Long, List<SymbolGapMap>> quotationFeed;
	
	public DataFeeder(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public void prepareFeed(IProcessingContext context) {
		synchronized (feedPrepared) {
			if (!feedPrepared) {
				logger.info("Loading trades");
				tickFeed = getTradeList(fromDate, toDate, context.getSymbols());
				logger.info("Loading quotes");
				quoteFeed = getQuoteList(fromDate, toDate, context.getSymbols());
				logger.info("Loading quotation");
				quotationFeed = getQuotationGapList(fromDate, toDate, context.getSymbols());	
				feedPrepared = true;
			}
		}
	}
	
	public static TreeMap<Long, List<TickTrade>> getTradeList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<TickTrade> ticks = DataManager.getTickList(dateFrom, dateTo, symbols);
		TreeMap<Long, List<TickTrade>> result = new TreeMap<>();
		for (TickTrade tick : ticks) {
			long time = tick.getTime().getTime();
			List<TickTrade> list = result.get(time);
			if (list == null) {
				list = new ArrayList<TickTrade>();
				result.put(time, list);
			}
			list.add(tick);
		}
		return result;
	}
	
	public static TreeMap<Long, List<Quote>> getQuoteList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<Quote> quotes = DataManager.getQuoteList(dateFrom, dateTo, symbols); // возвращает данные сплошным списком
		// разложим их по датам
		TreeMap<Long, List<Quote>> result = new TreeMap<>();
		for (Quote q : quotes) {
			List<Quote> list;
			long time = q.getTime().getTime();
			if (result.containsKey(time)) {
				list = result.get(time);
			} else {
				list = new ArrayList<Quote>();
			}
			list.add(q);
			result.put(time, list);
		}
		return result;
	}
	
	public static TreeMap<Long, List<SymbolGapMap>> getQuotationGapList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<SymbolGapMap> quotationGapList = DataManager.getQuotationGapList(dateFrom, dateTo, symbols);
		// разложим их по датам
		TreeMap<Long, List<SymbolGapMap>> result = new TreeMap<>();
		for (SymbolGapMap sg : quotationGapList) {
			List<SymbolGapMap> list;
			long time = sg.getTime().getTime();
			if (result.containsKey(time)) {
				list = result.get(time);
			} else {
				list = new ArrayList<SymbolGapMap>();
			}
			list.add(sg);
			result.put(time, list);
		}
		return result;
	}
	
	@Override
	public List<Candle> getCandleList(TQSymbol symbol, CandleType candleType,
			Date fromDate, int count) {
		Date toDate = new Date(fromDate.getTime());
		DateUtils.addDays(toDate, -1);
		return TQCandleService.getInstance().getSavedCandles(symbol, candleType, fromDate, toDate);
	}
	
	@Override
	public IInstrumentEvent<List<Quote>> getQuotesFeeder() {
		return quotesEvent;
	}
	
	@Override
	public IInstrumentEvent<List<Tick>> getTicksFeeder() {
		return ticksEvent;
	}

	@Override
	public IInstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder() {
		return quotationEvent;
	}
	

	@Override
	public void OnStart() {
		// Do nothing
	}

	public void feed(IProcessingContext context) {
				
		long tickTime = fromDate.getTime();
		threadTickTime.set(tickTime);
		long prevTickTime = 0;
		
		logger.debug("Feeding from " + Utils.formatDate(fromDate) + " to " + Utils.formatDate(toDate));

		prepareFeed(context);
		
		logger.debug("Starting tick " + context.getName());
		while (tickTime < toDate.getTime()) {			
			SortedMap<Long, List<Quote>> subQuotes = quoteFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, List<TickTrade>> subTicks = tickFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, List<SymbolGapMap>> subQuotations = quotationFeed.subMap(prevTickTime, true, tickTime, false);
			
			if (subQuotes.size() > 0) {
				for (List<Quote> quoteList : subQuotes.values()) {
					Map<TQSymbol, List<Quote>> map = TQQuoteService.createMap(quoteList);
					for (TQSymbol symbol : map.keySet()) {
						quotesEvent.notifyObservers(symbol, map.get(symbol));
					}
				}
			}
			
			if (subQuotations.size() > 0) {
				for (List<SymbolGapMap> symbolGap : subQuotations.values()) {
					Map<TQSymbol, List<SymbolGapMap>> map = TQQuotationService.createMap(symbolGap);
					for (TQSymbol symbol : map.keySet()) {
						quotationEvent.notifyObservers(symbol, map.get(symbol));
					}
				}
			}
				
			if (subTicks.size() > 0) {
				 List<TickTrade> subTicksTempList = new ArrayList<>();
				 for (List<TickTrade> l : subTicks.values()) {
					 subTicksTempList.addAll(l);
				 }
				 Map<TQSymbol, List<Tick>> map = TQTickTradeService.createMap(subTicksTempList);
				 for (TQSymbol symbol : map.keySet()) {
					 ticksEvent.notifyObservers(symbol, map.get(symbol));
				 }
			}

			
			prevTickTime = tickTime;
			tickTime += TICK_PERIOD;
			threadTickTime.set(tickTime);
		}

		logger.debug("Complete " + context.getName());
	}

	@Override
	public Date currentDate() {
		return new Date(threadTickTime.get());
	}

	static final double INITIAL_AMOUNT = 100000;
	
	ThreadLocal<IAccount> threadAccount = new ThreadLocal<IAccount>();
	
	@Override
	public IAccount getAccount() {
		IAccount account = threadAccount.get();
		if (account == null) {
			account = new SimpleAccount(INITIAL_AMOUNT, this);
			threadAccount.set(account);
		}
		return account;
	}
	
}
