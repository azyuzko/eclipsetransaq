package ru.eclipsetrader.transaq.core.candle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateUtils;

import ru.eclipsetrader.transaq.core.interfaces.ITQTickTrade;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.util.Utils;

public class CandleList {

	TreeMap<Date,Candle> map = new TreeMap<Date, Candle>();

	private CandleType candleType;
	
	//TODO refactor private Event<CandleList> onCandleListChange = new Event<CandleList>("CandleList.onCandleListChange");
	
	public CandleList(CandleType candleType){
		super();
		this.candleType = candleType;
		// System.out.println("CandleList " + candleType + " created..");
	}
	
	public CandleList(CandleType candleType, Collection<Candle> candles) {
		this(candleType);
		appendCandles(candles);
	}
	
	public Date[] dates() {
		return map.keySet().toArray(new Date[map.size()]);
	}
	
	public double[] values(PriceType priceType) {
		double[] values = new double[map.size()];
		int i = 0;
		for (Date date : map.keySet()) {
			values[i] = map.get(date).getPriceValueByType(priceType); 
			i++;
		}
		return values;
	}
	
	/**
	 * �������� ������ � ������ ������ �� ���� ������������
	 * @param toDate ���� ���� ����������
	 */
	public void truncHead(Date toDate) {
		Iterator<Date> itd = map.descendingKeySet().iterator();
		while (itd.hasNext()) {
			Date d = itd.next();
			if (!toDate.after(d)) {
				itd.remove();
			}
		}
	}
	
	/**
	 * �������� ����� � ������ ������ �� ���� ������������
	 * @param toDate ���� ���� ����������
	 */
	public void truncTail(Date toDate) {
		Iterator<Date> itd = map.descendingKeySet().iterator();
		while (itd.hasNext()) {
			Date d = itd.next();
			if (!toDate.before(d)) {
				itd.remove();
			}
		}
	}
	
	public CandleType getCandleType(){
		return candleType;
	}
	
	public Candle getLastCandle() {
		if (map.size() > 0) {
			return map.lastEntry().getValue();
		} else {
			return null;
		}
	}
	
	public void appendCandles(Collection<Candle> candles) {
		// System.out.println("appendCandles size = " + candles.size() + " to " + candleType);
		for (Candle candle : candles) {
			map.put(candle.getDate(), candle);
		}
	}
	
	public void putCandle(Candle candle) {
		map.put(candle.getDate(), candle);
	}
	
	/**
	 * ���� ��������� � ��������� ���� ������ ����� ��� ���������� �������
	 * ������� ������ ��� ���������� �� java-������������� :)
	 * @param toDate ����
	 * @param candleType ������ �����
	 * @return
	 */
	public static Date closestCandleStartTime(Date toDate, CandleType candleType) {
		long size = candleType.getSeconds();
		switch (candleType) {
		case CANDLE_1M: return DateUtils.truncate(toDate, Calendar.MINUTE);
		case CANDLE_1H: return DateUtils.truncate(toDate, Calendar.HOUR);
		case CANDLE_1D: return DateUtils.truncate(toDate, Calendar.DATE);
		case CANDLE_1W: return DateUtils.truncate(toDate, Calendar.WEEK_OF_MONTH);
		default:
			break;
		}
		Date result = DateUtils.truncate(toDate, candleType.getCalendarBase());
		long delta = toDate.getTime() - result.getTime();
		int mod = (int) (delta % (size * 1000));
		
		return DateUtils.addMilliseconds(toDate, -mod);
	}
	
	/**
	 * ������ ��� � �����
	 * @param trade
	 * @return
	 */
	public Candle processTradeInCandle(ITQTickTrade trade) {
		
		Candle topCandle = getLastCandle(); // ���� ������ ���, ������ null
		
		Date newTime = new Date(0); // minimal date
		
		if (topCandle != null) {
			newTime = new Date(topCandle.getDate().getTime() + (candleType.getSeconds()) * 1000 ); // ����������� �� ������ �����
			
			if (trade.getTime() == null) {
				throw new RuntimeException("trade.getTime() is null");
			}
		}
		
		if (trade.getTime().after(newTime)) {
			
			newTime = closestCandleStartTime(trade.getTime(), candleType);
			
			// System.out.println("New candle starting from " + Utils.formatDate(newTime));
			topCandle = new Candle();
			topCandle.setDate(newTime);
			map.put(trade.getTime(), topCandle);
		}
		
		double tradePrice =  trade.getPrice();
		if (tradePrice > topCandle.getHigh()) {
			topCandle.setHigh(tradePrice);
		}
		if (tradePrice < topCandle.getLow()) {
			topCandle.setLow(tradePrice);
		}
		if (topCandle.getOpen() == 0) {
			topCandle.setOpen(tradePrice);
		}
		topCandle.setClose(tradePrice);
		topCandle.setVolume(topCandle.getVolume() + trade.getQuantity());
		
		//TODO refactor onCandleListChange.notifyObservers(this);
		
		// System.out.println(topCandle.toString() );
		return topCandle;
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
		
		Date dt1 = Utils.parseDate("15.02.2015 21:37:56.145");
		System.out.println(Utils.formatDate(closestCandleStartTime(dt1, CandleType.CANDLE_1D)));

	/*	CandleList cl = new CandleList(CandleType.CANDLE_1H);
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
