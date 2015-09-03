package ru.eclipsetrader.transaq.core.candle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.util.Holder;
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
	
	ConcurrentSkipListMap<Date,Candle> map = new ConcurrentSkipListMap<Date, Candle>();
	
	public ListObserver<Tick> tickObserver = new ListObserver<Tick>() {
		@Override
		public void update(List<Tick> list) {
			if (logger.isDebugEnabled()) {
				logger.debug("iTickObserver " + list.get(0).getSeccode() + "  size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
			}
			list.forEach(t -> processTickInCandle(t));
		}
	};
	
	public ListObserver<Tick> getTickObserver() {
		return tickObserver;
	}

	public ICandleProcessContext getCandleProcessContext() {
		return candleProcessContext;
	}

	public void setCandleProcessContext(ICandleProcessContext candleProcessContext) {
		this.candleProcessContext = candleProcessContext;
	}

	public List<Candle> candleList() {
		return new ArrayList<>(map.values());
	}
	
	public Map<Date, Candle> candleMap() {
		return map;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public CandleList(TQSymbol symbol, CandleType candleType){
		this.symbol = symbol;
		this.candleType = candleType;
		logger.debug(symbol + " CandleList " + candleType + " created..");
	}
	
	public CandleList(TQSymbol symbol, CandleType candleType, Collection<Candle> candles) {
		this(symbol, candleType);
		appendCandles(candles);
	}
	
	public int size() {
		return map.size();
	}
	
	public Candle firstDayCandle() {
		return map.firstEntry().getValue();
	}
	
	public Holder<Date[], double[]> values(PriceType priceType) {
		return values(priceType, map.size());
	}
	
	public Holder<Date[], double[]> values(PriceType priceType, int count) {
		synchronized (map) {
			count = Math.min(count, map.size());
			double[] values = new double[count];
			int i = 1;
			for (Date date : map.descendingKeySet()) {
				values[count-i] = map.get(date).getPriceValueByType(priceType);
				i++;
				if (i > count) {
					break;
				}
			}
			Date[] dates = map.keySet().toArray(new Date[count]);
			return new Holder<Date[], double[]>(dates, values);
		}
	}
	
	public Date[] dates() {
		return dates(map.size());
	}
	
	public Date[] dates(int count) {
		synchronized (map) {
			count = Math.min(count, map.size());
			Date[] dates = map.keySet().toArray(new Date[count]);
			return dates;
		}
	}
	
	/**
	 * Отсекает голову у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
	 */
	public void truncHead(Date toDate) {
		synchronized (map) {
			Iterator<Date> itd = map.descendingKeySet().iterator();
			while (itd.hasNext()) {
				Date d = itd.next();
				if (!toDate.after(d)) {
					itd.remove();
				}
			}
		}
	}
	
	/**
	 * Отсекает хвост у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
	 */
	public void truncTail(Date toDate) {
		synchronized (map) {
			Iterator<Date> itd = map.descendingKeySet().iterator();
			while (itd.hasNext()) {
				Date d = itd.next();
				if (!toDate.before(d)) {
					itd.remove();
				}
			}			
		}
	}
	
	public CandleType getCandleType(){
		return candleType;
	}
	
	public Candle getLastCandle() {
		synchronized (map) {
			if (map.size() > 0) {
				return map.lastEntry().getValue();
			} else {
				return null;
			}			
		}
	}
	
	public void appendCandles(Collection<Candle> candles) {
		synchronized (map) {
			if (candles != null) {
				logger.debug(symbol + " appendCandles size = " + candles.size() + " to " + candleType);
				for (Candle candle : candles) {
					putCandle(candle);
				}
			}			
		}
	}
	
	public void putCandle(Candle candle) {
		synchronized (map) {
			map.put(candle.getDate(), candle);
			if (map.size() > maxSize) {
				map.remove(map.firstKey());
			}
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


	
	public void processTickInCandle(Tick tick) {
		synchronized (map) {
			if (tick.getTime() == null) {
				throw new IllegalArgumentException("trade.getTime() is null");
			}
			Candle topCandle = getLastCandle(); // если свечей нет, вернет null
			
			Date newTime = new Date(0); // minimal date
			
			if (topCandle != null) {
				newTime = new Date(topCandle.getDate().getTime() + (candleType.getSeconds()) * 1000 ); // перешагнули за размер свечи
				
			}
			
			if (tick.getTime().after(newTime)) {
				
				newTime = CandleList.closestCandleStartTime(tick.getTime(), candleType);
				
				if (topCandle != null && candleProcessContext != null) {
					candleProcessContext.onCandleClose(symbol, this, topCandle);
				}
				
				logger.debug("New candle starting from " + Utils.formatDate(newTime));
				topCandle = new Candle();
				topCandle.setDate(newTime);
				putCandle(topCandle);
			}
			topCandle.processTick(tick);		
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		for (Date date : map.navigableKeySet()) {
			sb.append(map.get(date).toString()); sb.append("\n");
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		
		Date dt1 = Utils.parseDate("15.02.2015 21:47:01.145");
		System.out.println(Utils.formatDate(closestCandleStartTime(dt1, CandleType.CANDLE_61S)));
		//System.out.println(DateUtils.ceiling(dt1, Calendar.MINUTE));

	/*	CandleList cl = new CandleList(CandleType.CANDLE_1M);
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
		
		cl.truncTail(Utils.parseDate("17.07.2015 00:00:00.000"));
		
		System.out.println("-- truncate --");
		
		System.out.println(cl.toString());*/
		
	}
	
}
