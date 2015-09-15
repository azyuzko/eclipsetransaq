package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.IPricingFeeder;
import ru.eclipsetrader.transaq.core.account.SimpleAccount;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.IInstrumentEvent;
import ru.eclipsetrader.transaq.core.event.SynchronousInstrumentEvent;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.strategy.IStrategy;
import ru.eclipsetrader.transaq.core.util.Holder3;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 * Кормилец данными из БД
 * @author Zyuzko-AA
 *
 */
public class DataFeeder implements IDataFeedContext {
	
	public enum FeedType {
		CANDLES,
		TICKS
	}
	
	Logger logger = LogManager.getLogger("DataFeeder");
	
	Date fromDate;
	Date toDate;
	TQSymbol[] symbols;

	static int TICK_FEED_PERIOD = 10; // период тика в милисекундах
	static int CANDLE_FEED_PERIOD = 1000; // период тика в милисекундах
	
	Boolean feedPrepared = false;
	
	ConcurrentSkipListMap<Long, List<TickTrade>> tickFeed;
	ConcurrentSkipListMap<Long, List<Quote>> quoteFeed;
	ConcurrentSkipListMap<Long, List<SymbolGapMap>> quotationFeed;
	
	ConcurrentSkipListMap<Long, Holder3<TQSymbol, CandleList, Candle>> candleFeed;
	
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
	
	public DataFeeder(Date fromDate, Date toDate, TQSymbol[] symbols) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.symbols = symbols;
	}
	

	private void initFeed(FeedType feedType) {
		if (!feedPrepared) {
			switch (feedType) {
			case CANDLES:
				candleFeed = getCandleFeed();
				break;
			case TICKS:
				initTicksFeed();
				break;
			default:
				break;
			}
			feedPrepared = true;
		}
	}
	
	public void initTicksFeed() {
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
		return ticks.stream().collect(Collectors.groupingBy( (TickTrade t) -> {return t.getReceived().getTime();}, ConcurrentSkipListMap::new, Collectors.toList()));
	}
		
	public static ConcurrentSkipListMap<Long, List<Quote>> getQuoteList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<Quote> quotes = DataManager.getQuoteList(dateFrom, dateTo, symbols);
		return quotes.stream().collect(Collectors.groupingBy( (Quote q) -> {return q.getTime().getTime();}, ConcurrentSkipListMap::new, Collectors.toList()));
	}
		
	public static ConcurrentSkipListMap<Long, List<SymbolGapMap>> getQuotationGapList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		List<SymbolGapMap> quotationGapList = DataManager.getQuotationGapList(dateFrom, dateTo, symbols);
		return quotationGapList.stream().collect(Collectors.groupingBy( (SymbolGapMap s) -> {return s.getTime().getTime();}, ConcurrentSkipListMap::new, Collectors.toList()));
	}
	
	@Override
	public List<Candle> getCandleList(TQSymbol symbol, CandleType candleType,
			Date toDate, int count) {
		Date fromDate = DateUtils.addDays(toDate, -1);
		return TQCandleService.getInstance().getSavedCandles(symbol, candleType, fromDate, toDate);
	}
	
	public ConcurrentSkipListMap<Long, Holder3<TQSymbol, CandleList, Candle>> getCandleFeed() {
		ConcurrentSkipListMap<Long, Holder3<TQSymbol, CandleList, Candle>> data = new ConcurrentSkipListMap<>();
		for (TQSymbol symbol : symbols) {
			for (CandleType candleType : new CandleType[] { // важен порядок!
					CandleType.CANDLE_1M, 
					CandleType.CANDLE_5M, 
					CandleType.CANDLE_15M }) {
				List<Candle> candles = TQCandleService.getInstance().getSavedCandles(symbol, candleType, fromDate, toDate);
				CandleList candleList = new CandleList(symbol, candleType, candles);
				for (Candle candle : candles) {
					Date candleEndDate = DateUtils.addSeconds(candle.getDate(), candleType.getSeconds());
					data.put(candleEndDate.getTime(), new Holder3<TQSymbol, CandleList, Candle>(symbol, candleList, candle));
				}
			}
		}
		return data;
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
	
	class QuoteGlassPricingFeeder implements IPricingFeeder {
		
		IStrategy strategy;
		
		public QuoteGlassPricingFeeder(IStrategy strategy) {
			this.strategy = strategy;
		}
		
		@Override
		public double sellPrice() {
			throw new IllegalArgumentException();
		}

		@Override
		public double buyPrice() {
			throw new IllegalArgumentException();
		}
	};
	
	class CandlesPricingFeeder implements IPricingFeeder {
		
		double price;
		
		public CandlesPricingFeeder(double price) {
			this.price = price;
		}
		
		@Override
		public double sellPrice() {
			return price;
		}
		
		@Override
		public double buyPrice() {
			return price;
		}
	};
		
	
	Object start = new Object();

	public void feed(FeedType feedType, IStrategy strategy, SimpleAccount account) {
		
		long tickTime = fromDate.getTime();
		long prevTickTime = 0;
		
		initFeed(feedType);
		
		logger.debug("Starting feed context = " + Integer.toHexString(strategy.hashCode()) + " from " + Utils.formatDate(fromDate) + " to " + Utils.formatDate(toDate));
				
		strategy.setDateTime(new Date(tickTime));
		
		if (feedType == FeedType.TICKS) {
			IPricingFeeder pricingFeeder = new QuoteGlassPricingFeeder(strategy);
			account.setPricingFeeder(pricingFeeder);
		}
		
		// strategy.start(account);
		
		logger.debug("Starting feed... ");
		while (tickTime < toDate.getTime()) {
			
			Date newDate = new Date(tickTime);
			strategy.setDateTime(newDate);
			
			if (feedType == FeedType.TICKS) {
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
				tickTime += TICK_FEED_PERIOD;

			} else if (feedType == FeedType.CANDLES) {

				NavigableMap<Long, Holder3<TQSymbol, CandleList, Candle>> subMap = candleFeed.subMap(prevTickTime, true, tickTime, false);
				
				if (subMap.size() > 0) {
					for (Holder3<TQSymbol, CandleList, Candle> value : subMap.values()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Time = " + Utils.formatTime(newDate) + "  " + value.getFirst() + " " + value.getSecond().getCandleType() + " " + value.getThird().getDate());
						}
						CandlesPricingFeeder candlePricingFeeder = new CandlesPricingFeeder(value.getThird().getClose());
						account.setPricingFeeder(candlePricingFeeder);
					}
				}
				
				prevTickTime = tickTime;
				tickTime += CANDLE_FEED_PERIOD;
			}

			
		}
		
		// strategy.stop();
		
		logger.debug("Complete " + strategy);
		
		// Remove threadlocals
		quotationEvent.remove();
		ticksEvent.remove();
		quotesEvent.remove();
		
	}

	public static void main(String[] args) {
		TQSymbol[] symbols = new TQSymbol[] {TQSymbol.BRV5};
		
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
		List<SymbolGapMap> list = DataManager.getQuotationGapList(fromDate, toDate, symbols);
		
		ConcurrentSkipListMap<Long, List<SymbolGapMap>> result = getQuotationGapList(fromDate, toDate, symbols);
		System.out.println(result.size());
	}

	
}
