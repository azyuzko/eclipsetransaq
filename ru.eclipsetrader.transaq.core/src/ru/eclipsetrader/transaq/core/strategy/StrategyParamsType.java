package ru.eclipsetrader.transaq.core.strategy;

import java.lang.reflect.Field;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.strategy.Strategy.WorkOn;

public class StrategyParamsType {

	TQSymbol watchSymbol;
	TQSymbol operSymbol;

	PriceType priceType;
	WorkOn workOn;
	CandleType candleType;
	int fast;
	int slow;
	int signal;
	
	public StrategyParamsType()  {
		
	}
	
	public StrategyParamsType(StrategyParamsType params) {
		if (params != null) {
			for (Field f : StrategyParamsType.class.getDeclaredFields()) {
				f.setAccessible(true);
				try {
					f.set(this, f.get(params));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public TQSymbol getWatchSymbol() {
		return watchSymbol;
	}

	public void setWatchSymbol(TQSymbol watchSymbol) {
		this.watchSymbol = watchSymbol;
	}

	public TQSymbol getOperSymbol() {
		return operSymbol;
	}

	public void setOperSymbol(TQSymbol operSymbol) {
		this.operSymbol = operSymbol;
	}

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public WorkOn getWorkOn() {
		return workOn;
	}

	public void setWorkOn(WorkOn workOn) {
		this.workOn = workOn;
	}

	public CandleType getCandleType() {
		return candleType;
	}

	public void setCandleType(CandleType candleType) {
		this.candleType = candleType;
	}

	public int getFast() {
		return fast;
	}

	public void setFast(int fast) {
		this.fast = fast;
	}

	public int getSlow() {
		return slow;
	}

	public void setSlow(int slow) {
		this.slow = slow;
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	public static void main(String[] args) {
		StrategyParamsType sp = new StrategyParamsType();
		sp.setFast(12);
		sp.setOperSymbol(TQSymbol.BRQ5);
		sp.setWorkOn(WorkOn.CandleClose);
		
		StrategyParamsType sp2 = new StrategyParamsType(sp);
		System.out.println(sp2.getWorkOn());
	}
}
