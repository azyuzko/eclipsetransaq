package ru.eclipsetrader.transaq.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.exception.UnimplementedException;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Candle {

	Date date;
	double open;
	double high;
	double low = Double.MAX_VALUE;
	double close;
	int volume;
	int oi; // open interest (for futures and options only) 
	
	// все операции лежат в этой структуре
	// используется для расчета других типов цен
	List<Holder<Double, Integer>> data = new ArrayList<Holder<Double, Integer>>();
	
	public List<Holder<Double, Integer>> getData() {
		return data;
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
		default:
			throw new UnimplementedException();
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

}
