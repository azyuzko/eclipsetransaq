package ru.eclipsetrader.transaq.core.strategy;

import java.lang.reflect.Field;

import com.tictactec.ta.lib.MAType;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.StrategyWorkOn;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public class StrategyParamsType {

	PriceType priceType;
	StrategyWorkOn workOn;
	CandleType candleType;
	CandleType shortCandleType;
	int fast;
	int slow;
	int signal;
	
	int stochF_optInFastK_Period;
	int stochF_optInFastD_Period;
	MAType stochF_optInFastD_MAType;
	
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

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public StrategyWorkOn getWorkOn() {
		return workOn;
	}

	public void setWorkOn(StrategyWorkOn workOn) {
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

	public CandleType getShortCandleType() {
		return shortCandleType;
	}

	public void setShortCandleType(CandleType shortCandleType) {
		this.shortCandleType = shortCandleType;
	}

	public static void main(String[] args) {
		StrategyParamsType sp = new StrategyParamsType();
		sp.setFast(12);
		
		StrategyParamsType sp2 = new StrategyParamsType(sp);
		System.out.println(sp2.getWorkOn());
	}
}
