package ru.eclipsetrader.transaq.core.trades;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateUtils;

import ru.eclipsetrader.transaq.core.account.FortsAccountSimulator;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IFortsQuotesSupplier;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.strategy.MACDStrategy;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 * Кормилец данными из БД
 * @author Zyuzko-AA
 *
 */
public class DataFeeder implements IDataFeedContext {
	
	Date fromDate;
	Date toDate;

	static int TICK_PERIOD = 1; // период тика в милисекундах
	
	InstrumentEvent<List<Tick>> ticksEvent = new InstrumentEvent<>("Tick DB emulator");
	InstrumentEvent<List<Quote>> quotesEvent = new InstrumentEvent<>("Quotes DB emulator");
	
	public DataFeeder(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public static TreeMap<Long, List<TickTrade>> getTradeList(Date dateFrom, Date dateTo) {
		List<TickTrade> ticks = DataManager.getTickList(dateFrom, dateTo);
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
	
	public static TreeMap<Long, List<Quote>> getQuoteList(Date dateFrom, Date dateTo) {
		List<Quote> quotes = DataManager.getQuoteList(dateFrom, dateTo); // возвращает данные сплошным списком
		// разложим их по датам
		TreeMap<Long, List<Quote>> result = new TreeMap<>();
		for (Quote q : quotes) {
			List<Quote> list;
			if (result.containsKey(q.getTime().getTime())) {
				list = result.get(q.getTime().getTime());
			} else {
				list = new ArrayList<Quote>();
			}
			list.add(q);
			result.put(q.getTime().getTime(), list);
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
	public InstrumentEvent<List<Quote>> getQuotesFeeder() {
		return quotesEvent;
	}
	
	@Override
	public InstrumentEvent<List<Tick>> getTicksFeeder() {
		return ticksEvent;
	}
	

	@Override
	public void OnStart() {
		// Do nothing
	}


	public void feed(IProcessingContext context) {
		long tickTime = this.fromDate.getTime();
		long prevTickTime = 0;
		
		TreeMap<Long, List<TickTrade>> tickFeed = getTradeList(fromDate, toDate);
		TreeMap<Long, List<Quote>> quoteFeed = getQuoteList(fromDate, toDate);		

		while (tickTime < toDate.getTime()) {			
			SortedMap<Long, List<Quote>> subQuotes = quoteFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, List<TickTrade>> subTicks = tickFeed.subMap(prevTickTime, true, tickTime, false);
			
			if (subQuotes.size() > 0) {
				for (List<Quote> quoteList : subQuotes.values()) {
					Map<TQSymbol, List<Quote>> map = TQQuoteService.createMap(quoteList);
					for (TQSymbol symbol : map.keySet()) {
						quotesEvent.notifyObservers(symbol, quoteList);
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
					 List<Tick> ticks = map.get(symbol);
					 ticksEvent.notifyObservers(symbol, ticks);
				 }
			}
		
			prevTickTime = tickTime;
			tickTime += TICK_PERIOD;
		}
	}
	
	IFortsQuotesSupplier quotesSupplier = new IFortsQuotesSupplier() {
		
		@Override
		public double getSellPrice(TQSymbol symbol) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getPrice(TQSymbol symbol) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getBuyPrice(TQSymbol symbol) {
			// TODO Auto-generated method stub
			return 0;
		}
	};

	@Override
	public IAccount getAccount(TQSymbol symbol) {
		FortsAccountSimulator account = new FortsAccountSimulator(quotesSupplier, 10000);
		return account;
	}
	
	public static void main(String[] args) throws IOException {
		Date fromDate = Utils.parseDate("29.07.2015 09:57:00.000");
		Date toDate = Utils.parseDate("29.07.2015 11:15:00.000");
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate);
		
		
		
		MACDStrategy macd = new MACDStrategy(dataFeeder);
		
		dataFeeder.feed(macd);

		System.out.println("Done!");
		// System.in.read();
	}


	
}
