package ru.eclipsetrader.transaq.core.candle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

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
		System.out.println("CandleList " + candleType + " created..");
	}
	
	public Date[] dates() {
		return map.values().toArray(new Date[map.size()]);
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
	 * Отсекает голову у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
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
	 * Отсекает хвост у списка свечей до даты включительно
	 * @param toDate дата тоже отсекается
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
		return map.lastEntry().getValue();
	}
	
	public void appendCandles(Collection<Candle> candles) {
		System.out.println("appendCandles size = " + candles.size() + " to " + candleType);
		for (Candle candle : candles) {
			map.put(candle.getDate(), candle);
		}
		//TODO refactor  onCandleListChange.notifyObservers(this);
	}
	
	public void putCandle(Candle candle) {
		map.put(candle.getDate(), candle);
	}
	
	public Candle processTradeInCandle(ITQTickTrade trade) {
		
		Candle topCandle = getLastCandle();
		
		Date newTime = new Date(topCandle.getDate().getTime() + (candleType.getSeconds()) * 1000 ); // перешагнули за размер свечи
		
		if (trade.getTime() == null) {
			throw new RuntimeException("trade.getTime() is null");
		}
		
		if (trade.getTime().after(newTime)) {
			
			Calendar cal = Calendar.getInstance(); // locale-specific
			cal.setTime(trade.getTime());
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			newTime = new Date(cal.getTimeInMillis());
			
			System.out.println("New candle starting from " + Utils.formatDate(newTime));
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
		CandleList cl = new CandleList(CandleType.CANDLE_1H);
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
		
		System.out.println(cl.toString());
		
	}
	
}
