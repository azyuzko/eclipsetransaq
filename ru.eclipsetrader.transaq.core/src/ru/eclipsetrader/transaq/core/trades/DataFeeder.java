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
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.strategy.MACDStrategy;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 *  ормилец данными из Ѕƒ
 * @author Zyuzko-AA
 *
 */
public class DataFeeder implements IDataFeedContext {
	
	Date fromDate;
	Date toDate;

	static int TICK_PERIOD = 1; // период тика в милисекундах
	
	InstrumentEvent<List<Tick>> ticksEvent = new InstrumentEvent<>("Tick DB emulator");
	InstrumentEvent<List<Quote>> quotesEvent = new InstrumentEvent<>("Quotes DB emulator");
	InstrumentEvent<List<SymbolGapMap>> quotationEvent = new InstrumentEvent<>("Quotation DB emulator");
	
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
	
	public static TreeMap<Long, List<SymbolGapMap>> getQuotationGapList(Date dateFrom, Date dateTo) {
		List<SymbolGapMap> quotationGapList = DataManager.getQuotationGapList(dateFrom, dateTo);
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
	public InstrumentEvent<List<Quote>> getQuotesFeeder() {
		return quotesEvent;
	}
	
	@Override
	public InstrumentEvent<List<Tick>> getTicksFeeder() {
		return ticksEvent;
	}

	@Override
	public InstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder() {
		return quotationEvent;
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
		TreeMap<Long, List<SymbolGapMap>> quotationFeed = getQuotationGapList(fromDate, toDate);		

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
		}
	}
	
	class FortsQuotesSupplier implements IFortsQuotesSupplier {
		
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
	
	FortsQuotesSupplier fortsQuotesSupplier = new FortsQuotesSupplier();

	@Override
	public IAccount getAccount(TQSymbol symbol) {
		FortsAccountSimulator account = new FortsAccountSimulator(fortsQuotesSupplier, 10000);
		return account;
	}
	
	public static void main(String[] args) throws IOException {
		Date fromDate = Utils.parseDate("30.07.2015 16:57:00.000");
		Date toDate = Utils.parseDate("30.07.2015 17:15:00.000");
		
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate);
		
		MACDStrategy macd = new MACDStrategy(dataFeeder);
		
		dataFeeder.feed(macd);
		
		//TreeMap<Long, List<SymbolGapMap>> map = getQuotationGapList(fromDate, toDate);
		//System.out.println("size = " + map.size());
		//for (List<SymbolGapMap> sgl : map.values()) {
		//	System.out.println(Utils.formatDate(sg.getTime() )+ " " + sg.getBoard() + " " + sg.getSeccode() + " " + SymbolGapMap.mapToString(sg.getGaps()));
		//}

		System.out.println("Done!");
		// System.in.read();
	}



	
}
