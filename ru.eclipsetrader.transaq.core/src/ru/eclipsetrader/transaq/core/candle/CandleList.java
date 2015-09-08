package ru.eclipsetrader.transaq.core.candle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Utils;

/**
 * График свечей
 * @author Zyuzko-AA
 *
 */
public class CandleList {
	
	int maxSize = 200; // макс.количество свечей в списке
	
	Logger logger = LogManager.getLogger("CandleList");
	
	TQSymbol symbol;
	CandleType candleType;
	ICandleProcessContext candleProcessContext;
	
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, Executors.defaultThreadFactory());
	
	final ConcurrentSkipListMap<Date,Candle> map = new ConcurrentSkipListMap<Date, Candle>();
	
	public ListObserver<Tick> tickObserver = (List<Tick> list) -> {
		if (logger.isDebugEnabled()) {
			logger.debug("iTickObserver " + list.get(0).getSeccode() + "  size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
		}
		list.forEach(t -> processTickInCandle(t));
	};
	
	public TQSymbol getSymbol() {
		return symbol;
	}
	
	public CandleType getCandleType() {
		return candleType;
	}
	
	public ListObserver<Tick> getTickObserver() {
		return tickObserver;
	}

	public ICandleProcessContext getCandleProcessContext() {
		return candleProcessContext;
	}

	public void setCandleProcessContext(ICandleProcessContext candleProcessContext) {
		this.candleProcessContext = candleProcessContext;
	}

	public synchronized Stream<Candle> stream(boolean desc) {
		if (desc) {
			return map.values().stream().sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate()));
		} else {
			return map.values().stream();
		}
	}
	
	public synchronized Stream<Candle> stream() {
		return stream(false);
	}
	
	public synchronized DoubleStream streamPrice(boolean desc, PriceType priceType) {
		return stream(desc).mapToDouble(c -> c.getPriceValueByType(priceType));
	}
	
	public synchronized DoubleStream streamPrice(PriceType priceType) {
		return stream().mapToDouble(c -> c.getPriceValueByType(priceType));
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public synchronized void clear() {
		map.clear();			
	}
	
	public CandleList(TQSymbol symbol, CandleType candleType){
		this.symbol = symbol;
		this.candleType = candleType;
		this.ses.scheduleAtFixedRate(() -> tickAtFixedTime(), 0, 1, TimeUnit.SECONDS);
		logger.debug(symbol + " CandleList " + candleType + " created..");
	}
	
	private void tickAtFixedTime() {
		logger.debug("Tick at fixed time");
		Date current = TransaqServer.getInstance() != null ? TransaqServer.getInstance().getServerTime() : new Date();
		Candle top = getLastCandle();
		// закрываем через 1 секунду
		if (top != null && current.after(DateUtils.addSeconds(top.getDate(), 1))) {
			processTickInCandle(current);
		}
	}
	
	public CandleList(TQSymbol symbol, CandleType candleType, Collection<Candle> candles) {
		this(symbol, candleType);
		appendCandles(candles);
	}
	
	public synchronized int size() {
		return map.size();
	}
	
	public synchronized Candle firstDayCandle() {
		return map.firstEntry().getValue();
	}
	
	/**
	 * Отсекает голову у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
	 */
	public synchronized void truncHead(Date toDate) {
		Objects.requireNonNull(toDate);
		map.descendingKeySet().stream().filter((d) -> !toDate.after(d)).forEach(d -> map.remove(d));
	}
	
	/**
	 * Отсекает хвост у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
	 */
	public synchronized void truncTail(Date toDate) {
		Objects.requireNonNull(toDate);
		map.descendingKeySet().stream().filter((d) -> !toDate.before(d)).forEach(d -> map.remove(d));
	}
	
	public synchronized Candle getLastCandle() {
		if (map.size() > 0) {
			return map.lastEntry().getValue();
		} else {
			return null;
		}			
	}
	
	public synchronized void appendCandles(Collection<Candle> candles) {
		Objects.requireNonNull(candles);
		logger.debug(symbol + " appendCandles size = " + candles.size() + " to " + candleType);
		candles.forEach(c -> putCandle(c));
	}
	
	public synchronized void putCandle(Candle candle) {
		map.put(candle.getDate(), candle);
		if (map.size() > maxSize) {
			map.remove(map.firstKey());
		}
	}
	
	/**
	 * Ищет ближайшую к указанной дату начала свечи для указанного размера
	 * Хорошая задача для кандидатов на java-разработчиков :)
	 * 
	 * Для некратных размеров пример:
	 * 21:47:30.145 CANDLE_16S = 21:47:15.000
	 * 21:47:31.145 CANDLE_16S = 21:47:30.000
	 * 
	 * 
	 * @param toDate дата
	 * @param candleType размер свечи
	 * @return
	 */
	public static Date closestCandleStartTime(Date toDate, CandleType candleType) {
		int size = candleType.getSeconds();
		switch (candleType) {
		case CANDLE_1M: return DateUtils.truncate(toDate, Calendar.MINUTE);
		case CANDLE_1H: return DateUtils.truncate(toDate, Calendar.HOUR);
		case CANDLE_1D: return DateUtils.truncate(toDate, Calendar.DATE);
		case CANDLE_1W: return DateUtils.truncate(toDate, Calendar.WEEK_OF_MONTH);
		default:
			break;
		}
		
		CandleType rounded60CandleType = candleType.getClosest60Candle();
		// Если свеча некратного размера
		if (rounded60CandleType != candleType) {
			Date truncatedDate = DateUtils.truncate(toDate, rounded60CandleType.getCalendarBase());
			long ms_delta = toDate.getTime() - truncatedDate.getTime();
			int s_rounded_size = rounded60CandleType.getSeconds();
			int s_rounded_delta = size - s_rounded_size;
			int ms_mod = (int) (ms_delta % (s_rounded_size * 1000));
			if (ms_mod < s_rounded_delta * 1000) {
				return DateUtils.addMilliseconds(toDate, -ms_mod - s_rounded_size*1000);
			} else {
				return DateUtils.addMilliseconds(toDate, -ms_mod);
			}
		} else {
			Date truncatedDate = DateUtils.truncate(toDate, candleType.getCalendarBase());
			long ms_delta = toDate.getTime() - truncatedDate.getTime();
			int ms_mod = (int) (ms_delta % (size * 1000));
			return DateUtils.addMilliseconds(toDate, -ms_mod);
		}
	}

	
	public void processTickInCandle(Date date) {
		processTickInCandle(date, null);
	}
	
	public void processTickInCandle(Tick tick) {
		processTickInCandle(tick.getTime(), tick);
	}
	
	private synchronized void processTickInCandle(Date time, Tick tick) {
		Objects.requireNonNull(time);
		
		Candle topCandle = getLastCandle(); // если свечей нет, вернет null
		
		Date newTime = new Date(0); // minimal date
		
		if (topCandle != null) {
			newTime = new Date(topCandle.getDate().getTime() + (candleType.getSeconds()) * 1000 ); // перешагнули за размер свечи
		}
		
		if (time.after(newTime)) {
			if (topCandle != null && !topCandle.isClosed()) {
				topCandle.setCloseDate(newTime);
				if (candleProcessContext != null) {
					candleProcessContext.onCandleClose(this, topCandle);
				}
			}
			
			// если была сделка, надо открыть новую свечу
			if (tick != null) {
				newTime = CandleList.closestCandleStartTime(time, candleType);
				logger.debug("New candle starting from " + Utils.formatDate(newTime));
				topCandle = new Candle();
				topCandle.setDate(newTime);
				putCandle(topCandle);
			}
		} else {
			if (topCandle.getDate().after(time)) {
				logger.warn("Unexpected problem. Tick time <"  + Utils.formatDate(time) + "> less than topCandle time <" + Utils.formatDate(topCandle.getDate()) + ">");
			}
		}
		
		if (topCandle != null && tick != null) {
			topCandle.processTick(tick);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		map.navigableKeySet().stream().forEachOrdered((date) -> { sb.append(map.get(date) + "\n"); });
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		
		Date dt1 = Utils.parseDate("15.02.2015 21:47:01.145");
		System.out.println(Utils.formatDate(closestCandleStartTime(dt1, CandleType.CANDLE_61S)));
		//System.out.println(DateUtils.ceiling(dt1, Calendar.MINUTE));

		CandleList cl = new CandleList(TQSymbol.BRU5, CandleType.CANDLE_1M);
		Candle c1 = new Candle();
		c1.setDate(Utils.parseDate("16.07.2015 23:00:00.000"));
		c1.setOpen(1.2);
		
		Candle c2 = new Candle();
		c2.setDate(Utils.parseDate("17.07.2015 00:00:00.000"));
		c2.setOpen(1.2);

		Candle c3 = new Candle();
		c3.setDate(Utils.parseDate("17.07.2015 01:00:00.000"));
		c3.setOpen(1.2);
		
		cl.putCandle(c1);
		cl.putCandle(c2);
		cl.putCandle(c3);
		
		System.out.println(cl.toString());
		
	/*	System.out.println("-- truncate Tail --");
		cl.truncTail(Utils.parseDate("17.07.2015 00:00:00.000"));
		System.out.println(cl.toString());
		
		System.out.println("-- truncate Head --");
		cl.clear();
		cl.putCandle(c1);
		cl.putCandle(c2);
		cl.putCandle(c3);		
		cl.truncHead(Utils.parseDate("17.07.2015 00:00:00.000"));
		System.out.println(cl.toString());*/
		
		System.out.println("---");
		cl.stream().forEach(System.out::println);
		System.out.println("---");
		

	}
	
}
