package ru.eclipsetrader.transaq.core.candle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.exception.UnimplementedException;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Candle {
	
	public interface ICandleCalculator {
		double getPrice();
	}

	Date date;
	double open;
	double high;
	double low = Double.MAX_VALUE;
	double close;
	int volume;
	int oi; // open interest (for futures and options only) 
	
	// все тики свечи лежат в этой структуре
	// используется для расчета других типов цен
	TreeMap<Date, List<Holder<Double, Integer>>> ticks = new TreeMap<Date, List<Holder<Double, Integer>>>();

	public void processTick(Tick tick) {
		Date date = tick.getTime();
		double tickPrice = tick.getPrice();

		if (tickPrice > high) {
			high = tickPrice;
		}
		if (tickPrice < low) {
			low = tickPrice;
		}
		if (open == 0) {
			open = tickPrice;
		}
		if (close != tickPrice) {
			close = tickPrice;
		}
		
		volume += tick.getQuantity();

		List<Holder<Double, Integer>> list = ticks.get(date);
		if (list == null) {
			list = new ArrayList<Holder<Double,Integer>>();
			ticks.put(date, list);
		}
		list.add(new Holder<Double, Integer>(tickPrice, tick.getQuantity()));
	}

	@Override
	public String toString() {
		return "date = " + Utils.formatDate(date) + ",   open = " + open + ",   high = " + high + ",   low = " + low + ",   close = " + close + ",   volume = " + volume;
	}
	
	public Candle() {
	
	}
	
	public double getPriceValueByType(PriceType priceType) {
		switch (priceType) {
		case OPEN: return getOpen();
		case CLOSE: return getClose();
		case HIGH: return getHigh();
		case LOW: return getLow();
		case MED: return (getHigh() + getLow()) / 2;
		//case TYPICAL: return (getHigh() + getLow() + getClose()) / 3; вообще ни о чем
		case WEIGHTED_CLOSE: return (getHigh() + getLow() + 2 * getClose()) / 4;
		case VOLUME_WEIGHTED: {
			double full = 0;
			for (Date tickDate : ticks.keySet()) {
				for (Holder<Double, Integer> x : ticks.get(tickDate)) {
					full += x.getFirst() * x.getSecond();
				}
			}
			return full / getVolume();
		}
		default:
			throw new UnimplementedException();
		}
	}
	
	public CandleColor getCandleColor() {
		if (open > close) {
			return CandleColor.BLACK;
		} else if (open < close) {
			return CandleColor.WHITE;
		} else {
			return CandleColor.NO_COLOR;
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getOi() {
		return oi;
	}

	public void setOi(int oi) {
		this.oi = oi;
	}
	
	public static void main(String[] args) {
		Candle c = new Candle();
		c.open = 50.0;
		c.low = 40.0;
		c.high = 70.0;
		c.close = 60.0;
		c.volume = 12;
		
		System.out.println(c.getPriceValueByType(PriceType.MED));
		System.out.println(c.getPriceValueByType(PriceType.VOLUME_WEIGHTED));
		//System.out.println(c.getPriceValueByType(PriceType.TYPICAL));
		System.out.println(c.getPriceValueByType(PriceType.WEIGHTED_CLOSE));
	}

}
