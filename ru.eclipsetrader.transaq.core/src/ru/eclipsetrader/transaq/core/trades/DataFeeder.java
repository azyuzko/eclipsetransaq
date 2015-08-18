package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.account.SimpleAccount;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.IInstrumentEvent;
import ru.eclipsetrader.transaq.core.event.SynchronousInstrumentEvent;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.strategy.IStrategy;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 *  ормилец данными из Ѕƒ
 * @author Zyuzko-AA
 *
 */
public class DataFeeder implements IDataFeedContext {
	

	static final double INITIAL_AMOUNT = 100000;
	
	
	Logger logger = LogManager.getLogger("DataFeeder");
	
	Date fromDate;
	Date toDate;

	static int TICK_PERIOD = 10; // период тика в милисекундах
	
	ThreadLocal<SynchronousInstrumentEvent<List<Tick>>> ticksEvent = new ThreadLocal<SynchronousInstrumentEvent<List<Tick>>>() {
		@Override
		protected SynchronousInstrumentEvent<List<Tick>> initialValue() {
			return new SynchronousInstrumentEvent<List<Tick>>("Tick DB emulator");
		}
	};
	ThreadLocal<SynchronousInstrumentEvent<List<Quote>>> quotesEvent = new ThreadLocal<SynchronousInstrumentEvent<List<Quote>>>() {
		@Override
		protected SynchronousInstrumentEvent<List<Quote>> initialValue() {
			return new SynchronousInstrumentEvent<>("Quotes DB emulator");
		}
	};
	ThreadLocal<SynchronousInstrumentEvent<List<SymbolGapMap>>> quotationEvent = new ThreadLocal<SynchronousInstrumentEvent<List<SymbolGapMap>>>(){
		@Override
		protected SynchronousInstrumentEvent<List<SymbolGapMap>> initialValue() {
			return new SynchronousInstrumentEvent<List<SymbolGapMap>>("Quotation DB emulator");
		}
	};
	
	Boolean feedPrepared = false;
	
	ConcurrentSkipListMap<Long, List<TickTrade>> tickFeed;
	ConcurrentSkipListMap<Long, List<Quote>> quoteFeed;
	ConcurrentSkipListMap<Long, List<SymbolGapMap>> quotationFeed;

	class QuotesTask implements Callable<ConcurrentSkipListMap<Long, List<Quote>>> {
		Date fromDate;
		Date toDate;
		TQSymbol[] symbols;
		QuotesTask (Date fromDate, Date toDate, TQSymbol[] symbols) {
			this.fromDate = fromDate;
			this.toDate = toDate;
			this.symbols = symbols;
		}
		@Override
		public ConcurrentSkipListMap<Long, List<Quote>> call()
				throws Exception {
			logger.info("Loading quotes from " + Utils.formatDate(fromDate) + " to " + Utils.formatDate(toDate) + "...");
			ConcurrentSkipListMap<Long, List<Quote>> result = getQuoteList(fromDate, toDate, symbols);
			logger.info("Loaded quoteFeed from " + Utils.formatDate(fromDate) + " to " + Utils.formatDate(toDate) + " " + result.size());
			return result;
		}
	}
	
	public DataFeeder(final Date fromDate, final Date toDate, final TQSymbol[] symbols) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		
		ExecutorService es = Executors.newFixedThreadPool(5);
		
		Future<ConcurrentSkipListMap<Long, List<TickTrade>>> tradesTask = es.submit(new Callable<ConcurrentSkipListMap<Long, List<TickTrade>>>() {
			@Override
			public ConcurrentSkipListMap<Long, List<TickTrade>> call()
					throws Exception {
				logger.info("Loading trades...");
				ConcurrentSkipListMap<Long, List<TickTrade>> result = getTradeList(fromDate, toDate, symbols);
				logger.info("Loaded tickFeed " + result.size());
				return result;
			}
		});
		
		Date tmpDate = new Date((fromDate.getTime() + toDate.getTime())/2);
		QuotesTask qt = new QuotesTask(fromDate, toDate, symbols);
		QuotesTask qt1 = new QuotesTask(fromDate, tmpDate, symbols);
		QuotesTask qt2 = new QuotesTask(tmpDate, toDate, symbols);
		
		Future<ConcurrentSkipListMap<Long, List<Quote>>> quotesTask1 = es.submit(qt1);
		Future<ConcurrentSkipListMap<Long, List<Quote>>> quotesTask2 = es.submit(qt2);
		
		Future<ConcurrentSkipListMap<Long, List<SymbolGapMap>>> quotationTask = es.submit(new Callable<ConcurrentSkipListMap<Long, List<SymbolGapMap>>>() {
			@Override
			public ConcurrentSkipListMap<Long, List<SymbolGapMap>> call()
					throws Exception {
				logger.info("Loading quotation...");
				ConcurrentSkipListMap<Long, List<SymbolGapMap>> result = getQuotationGapList(fromDate, toDate, symbols);	
				logger.info("Loaded quotationFeed " + result.size());
				return result;
			}
		});
		
		try {
			long start = System.currentTimeMillis();
			tickFeed = tradesTask.get();
			ConcurrentSkipListMap<Long, List<Quote>> quoteFeed1 = quotesTask1.get();
			ConcurrentSkipListMap<Long, List<Quote>> quoteFeed2 = quotesTask2.get();
			quoteFeed = new ConcurrentSkipListMap<>();
			quoteFeed.putAll(quoteFeed1);
			quoteFeed.putAll(quoteFeed2);
			quotationFeed = quotationTask.get();
			long end = System.currentTimeMillis();
			
			if (quoteFeed.size() != (quoteFeed1.size() + quoteFeed2.size())) {
				throw new RuntimeException("Wrong quotes size");
			}
			
			logger.info("Load complete in " + (end - start)/1000 + " sec");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			es.shutdown();
		}
	}
		
	public static ConcurrentSkipListMap<Long, List<TickTrade>> getTradeList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<TickTrade> ticks = DataManager.getTickList(dateFrom, dateTo, symbols);
		ConcurrentSkipListMap<Long, List<TickTrade>> result = new ConcurrentSkipListMap<>();
		for (TickTrade tick : ticks) {
			long time = tick.getReceived().getTime();
			List<TickTrade> list = result.get(time);
			if (list == null) {
				list = new ArrayList<TickTrade>();
				result.put(time, list);
			}
			list.add(tick);
		}
		return result;
	}
	
	public static ConcurrentSkipListMap<Long, List<Quote>> getQuoteList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<Quote> quotes = DataManager.getQuoteList(dateFrom, dateTo, symbols); // возвращает данные сплошным списком
		// разложим их по датам
		ConcurrentSkipListMap<Long, List<Quote>> result = new ConcurrentSkipListMap<>();
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
	
	public static ConcurrentSkipListMap<Long, List<SymbolGapMap>> getQuotationGapList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<SymbolGapMap> quotationGapList = DataManager.getQuotationGapList(dateFrom, dateTo, symbols);
		// разложим их по датам
		ConcurrentSkipListMap<Long, List<SymbolGapMap>> result = new ConcurrentSkipListMap<>();
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
			Date toDate, int count) {
		Date fromDate = DateUtils.addDays(toDate, -1);
		// TODO return TQCandleService.getInstance().getSavedCandles(symbol, candleType, fromDate, toDate);
		return new ArrayList<Candle>();
	}
	
	@Override
	public IInstrumentEvent<List<Quote>> getQuotesFeeder() {
		return quotesEvent.get();
	}
	
	@Override
	public IInstrumentEvent<List<Tick>> getTicksFeeder() {
		return ticksEvent.get();
	}

	@Override
	public IInstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder() {
		return quotationEvent.get();
	}
	
	Object s = new Object();

	@Override
	public void OnStart(Instrument[] instruments) {
		synchronized (s) {
			for (Instrument i : instruments) {
				logger.debug("On start " + i.getSymbol() + " " + Integer.toHexString(i.hashCode()));
				i.init(fromDate);
				logger.debug("ticksEvent.get() = " + ticksEvent.get());
			}
		}
	}

	public void feed(IStrategy strategy) {
		
		long tickTime = fromDate.getTime();
		long prevTickTime = 0;
		
		logger.debug("Starting feed context = " + Integer.toHexString(strategy.hashCode()) + " from " + Utils.formatDate(fromDate) + " to " + Utils.formatDate(toDate));
		
		HashMap<TQSymbol, QuantityCost> initPositions = new HashMap<>();
		initPositions.put(TQSymbol.SiU5, new QuantityCost(1, 0));
		SimpleAccount sa = new SimpleAccount(INITIAL_AMOUNT, strategy, initPositions);
		
		strategy.setDateTime(new Date(tickTime));
		strategy.start(sa);
		
		logger.debug("Starting feed... ");
		while (tickTime < toDate.getTime()) {
			
			Date newDate = new Date(tickTime);
			strategy.setDateTime(newDate);
			
			SortedMap<Long, List<Quote>> subQuotes = quoteFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, List<TickTrade>> subTicks = tickFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, List<SymbolGapMap>> subQuotations = quotationFeed.subMap(prevTickTime, true, tickTime, false);
			
			if (subQuotes.size() > 0) {
				logger.debug("Time = " + Utils.formatTime(newDate) +" subQuotes.size = " + subQuotes.size());
				for (List<Quote> quoteList : subQuotes.values()) {
					Map<TQSymbol, List<Quote>> map = TQQuoteService.createMap(quoteList);
					for (TQSymbol symbol : map.keySet()) {
						quotesEvent.get().notifyObservers(symbol, map.get(symbol));
					}
				}
			}
			
			if (subQuotations.size() > 0) {
				logger.debug("Time = " + Utils.formatTime(newDate) +" subQuotations.size = " + subQuotations.size());
				for (List<SymbolGapMap> symbolGap : subQuotations.values()) {
					Map<TQSymbol, List<SymbolGapMap>> map = TQQuotationService.createMap(symbolGap);
					for (TQSymbol symbol : map.keySet()) {
						quotationEvent.get().notifyObservers(symbol, map.get(symbol));
					}
				}
			}
				
			if (subTicks.size() > 0) {
				logger.debug("Time = " + Utils.formatTime(newDate) +" subTicks.size = " + subTicks.size());
				 List<TickTrade> subTicksTempList = new ArrayList<>();
				 for (List<TickTrade> l : subTicks.values()) {
					 subTicksTempList.addAll(l);
				 }
				 Map<TQSymbol, List<Tick>> map = TQTickTradeService.createMap(subTicksTempList);
				 for (TQSymbol symbol : map.keySet()) {
					 ticksEvent.get().notifyObservers(symbol, map.get(symbol));
				 }
			}

			
			prevTickTime = tickTime;
			tickTime += TICK_PERIOD;
		}
		strategy.closePositions();
		strategy.stop();
		logger.debug("Complete " + strategy);
		
		// Remove threadlocals
		quotationEvent.remove();
		ticksEvent.remove();
		quotesEvent.remove();
		
	}

	
}
