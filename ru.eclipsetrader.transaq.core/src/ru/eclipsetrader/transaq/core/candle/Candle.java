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

import com.google.common.base.MoreObjects;

public class Candle {
	
	Date date;
	Date closeDate;
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
		ticks.computeIfAbsent(date, p -> new ArrayList<>()).add(new Holder<Double, Integer>(tickPrice, tick.getQuantity()));
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).
				add("date", Utils.formatDate(date)).add("open", open).add("high", high).add("low", low).add("close", close).add("volume", volume).toString();
	}
	
	public Candle() {
	
	}
	
	public double getPriceValueByType(PriceType priceType) {
		switch (priceType) {
		case OPEN: return getOpen();
		case CLOSE: return getClose();
		case HIGH: return getHigh();
		case LOW: return getLow();
		case MED: return (getHigh() + getLow() +  getClose()) / 3;
		case WEIGHTED_CLOSE: return (getHigh() + getLow() + 2 * getClose()) / 4;
		case VOLUME_WEIGHTED: {
			double sum = ticks.entrySet().stream().map( p -> p.getValue() ).reduce(new ArrayList<Holder<Double, Integer>>(), (a,b) -> { a.addAll(b); return a;}).stream()
					.mapToDouble(p -> p.getFirst() * p.getSecond() ).sum();
			return sum / getVolume();
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Candle) {
			Candle c = (Candle)obj;
			return c.getDate().equals(getDate()) && c.getClose() == getClose() && c.getOpen() == getOpen() && c.getHigh() == getHigh() && c.getLow() == getLow() && c.getVolume() == getVolume();
		} else {
			return false;
		}
	}
	
	public boolean isClosed() {
		return closeDate != null;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public Candle setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
		return this;
	}

	public Date getDate() {
		return date;
	}

	public Candle setDate(Date date) {
		this.date = date;
		return this;
	}

	public double getOpen() {
		return open;
	}

	public Candle setOpen(double open) {
		this.open = open;
		return this;
	}

	public double getHigh() {
		return high;
	}

	public Candle setHigh(double high) {
		this.high = high;
		return this;
	}

	public double getLow() {
		return low;
	}

	public Candle setLow(double low) {
		this.low = low;
		return this;
	}

	public double getClose() {
		return close;
	}

	public Candle setClose(double close) {
		this.close = close;
		return this;
	}

	public int getVolume() {
		return volume;
	}

	public Candle setVolume(int volume) {
		this.volume = volume;
		return this;
	}

	public int getOi() {
		return oi;
	}

	public Candle setOi(int oi) {
		this.oi = oi;
		return this;
	}

}
