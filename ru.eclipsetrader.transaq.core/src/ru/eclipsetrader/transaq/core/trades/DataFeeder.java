package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 * Кормилец данными из БД
 * @author Zyuzko-AA
 *
 */
public class DataFeeder {
	
	Date fromDate;
	Date toDate;
	
	TreeMap<Long, TickTrade> tickFeed;
	TreeMap<Long, List<Quote>> quoteFeed;

	
	public DataFeeder(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public static TreeMap<Long, TickTrade> getTradeList(Date dateFrom, Date dateTo) {
		@SuppressWarnings("unchecked")
		List<TickTrade> ticks = DataManager.executeQuery("select t from TickTrade t where t.time between :from and :to order by t.time",
				new Holder<String, Object>("from", dateFrom), new Holder<String, Object>("to", dateTo)
				);
		TreeMap<Long, TickTrade> result = new TreeMap<>();
		for (TickTrade tick : ticks) {
			result.put(tick.getTime().getTime(), tick);
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
	
	
	static int TICK_PERIOD = 1; // период тика в милисекундах

	public void feed(IDataFeedContext context) {
		long tickTime = this.fromDate.getTime();
		long prevTickTime = 0;
		
		tickFeed = getTradeList(fromDate, toDate);
		quoteFeed = getQuoteList(fromDate, toDate);

		while (tickTime < toDate.getTime()) {			
			SortedMap<Long, List<Quote>> subQuotes = quoteFeed.subMap(prevTickTime, true, tickTime, false);
			SortedMap<Long, TickTrade> subTicks = tickFeed.subMap(prevTickTime, true, tickTime, false);
			
			if (subQuotes.size() > 0) {
				for (List<Quote> quoteList : subQuotes.values()) {
					context.processQuoteList(new Date(tickTime), quoteList);
				}
			}
			
			if (subTicks.size() > 0) {
				for (TickTrade tick : subTicks.values()) {
					context.processTickTrade(new Date(tickTime), tick);
				}
			}
		
			prevTickTime = tickTime;
			tickTime += TICK_PERIOD;
		}
	}
	
/*
	public void feed(Instrument i, IProcessingContext signalEventHandler) {
		TickTrade tickTrade;
		//System.out.println("start feed");
		while ((tickTrade = getNext()) != null) {
			i.processTrade(tickTrade);
			signalEventHandler.completeTrade(tickTrade, i);
		}
		signalEventHandler.complete(i);
		//System.out.println("end feed");
	}*/
	
	public static void main(String[] args) {
		Date fromDate = Utils.parseDate("22.07.2015 19:15:00.000");
		Date toDate = Utils.parseDate("22.07.2015 21:30:00.000");
		DataFeeder df = new DataFeeder(fromDate, toDate);
		df.feed(new IDataFeedContext() {
			
			@Override
			public void processTickTrade(Date tickTime, Tick tick) {
				System.out.println(Utils.formatTime(tickTime) + "  Tick: " + tick.getTradeno() + " " + tick.getSeccode() + " "+ Utils.formatTime(tick.getTime()));
			}
			
			@Override
			public void processQuoteList(Date tickTime, List<Quote> quoteList) {
				System.out.println(Utils.formatTime(tickTime) + "  Quote: " + quoteList.size() + " " + Utils.formatTime(quoteList.get(0).getTime()));
			}
		});
	}

	
}
