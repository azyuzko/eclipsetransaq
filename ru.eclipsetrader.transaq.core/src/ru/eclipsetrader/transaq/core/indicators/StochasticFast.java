package ru.eclipsetrader.transaq.core.indicators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.account.SimpleAccount;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.strategy.StrategyPosition;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

/**
 * Медленный стохастик 
 * @author zar
 *
 */
public class StochasticFast extends IndicatorFunction {


	
	int optInFastK_Period = 5;
	int optInFastD_Period = 3;
	MAType optInFastD_MAType = MAType.Sma;

	double[] inHigh;
	double[] inLow;
	double[] inClose;
	
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	
	double[] outFastK;
	double[] outFastD;

	public StochasticFast() {
		lookback = core.stochFLookback(optInFastK_Period, optInFastD_Period, optInFastD_MAType);
	}
	
	public StochasticFast(int optInFastK_Period, int optInFastD_Period, MAType optInFastD_MAType ) {
		this.optInFastK_Period = optInFastK_Period;
		this.optInFastD_Period = optInFastD_Period;
		this.optInFastD_MAType = optInFastD_MAType;
		lookback = core.stochFLookback(optInFastK_Period, optInFastD_Period, optInFastD_MAType);
	}

	public void evaluate(CandleList cl) {
		int startIdx = 0;
		int endIdx = cl.size() - 1;

		inHigh = cl.values(PriceType.HIGH).getSecond();
		inLow = cl.values(PriceType.LOW).getSecond();
		inClose = cl.values(PriceType.CLOSE).getSecond();

		outFastK = new double[cl.size()];
		outFastD = new double[cl.size()];
		core.stochF(startIdx, endIdx, inHigh, inLow, inClose, optInFastK_Period, optInFastD_Period, optInFastD_MAType, outBegIdx, outNBElement, outFastK, outFastD);
		normalizeArray(outFastK, lookback);
		normalizeArray(outFastD, lookback);
	}

	public double[] getInHigh() {
		return inHigh;
	}

	public double[] getInLow() {
		return inLow;
	}

	public double[] getInClose() {
		return inClose;
	}

	public int getOptInFastK_Period() {
		return optInFastK_Period;
	}

	public void setOptInFastK_Period(int optInFastK_Period) {
		this.optInFastK_Period = optInFastK_Period;
	}

	public Integer getOutBegIdx() {
		if (outBegIdx != null) {
			return outBegIdx.value;	
		} else {
			return null;
		}
	}

	public Integer getOutNBElement() {
		if (outNBElement != null) {
			return outNBElement.value;
		} else {
			return null;
		}
	}

	public int getOptInFastD_Period() {
		return optInFastD_Period;
	}

	public MAType getOptInFastD_MAType() {
		return optInFastD_MAType;
	}

	public double[] getOutFastK() {
		return outFastK;
	}

	public double[] getOutFastD() {
		return outFastD;
	}
	
	public static String simpleStochFTest(CandleList cl, SimpleAccount sa, StochasticFast sf) {
		TQSymbol symbol = TQSymbol.SiU5;
		
		sf.evaluate(cl);
		Date[] dates = cl.dates();
				
		double[] close = cl.values(PriceType.CLOSE).getSecond();
		double[] fastK = sf.getOutFastK();
		double[] fastD = sf.getOutFastD();
		double[] diff = Utils.minusArray(fastK, fastD);
		
		StringBuilder sb = new StringBuilder();
		
		StrategyPosition position = null;
		
		for (int i = 0; i < dates.length-1; i++) {
			if (fastD[i] > 0 && fastK[i] > 0) {
				
				BuySell bs = null;
				if ((position == null || position.getBuySell() == BuySell.S) && fastK[i] < 15) {
					if (sa.buy(symbol, 1, close[i]).getQuantity() > 0) {
						position = new StrategyPosition(symbol, BuySell.B);
					};
				}
				
				if ((position == null || position.getBuySell() == BuySell.B) && fastK[i] > 85) {
					if (sa.sell(symbol, 1, close[i]).getQuantity() > 0) {
						position = new StrategyPosition(symbol, BuySell.S);
					}
				}
				
				if (position != null) {
					if (position.getBuySell() == BuySell.B) {
						if (diff[i] > 20) {
							if (sa.sell(symbol, 1, close[i]).getQuantity() > 0) {
								bs = BuySell.S;
								position = null;
							}							
						}
					}

				}

				String s = String.format(
						"\n %s :  %5.2f  %5.2f  %5.2f  %5.2f %s",
						Utils.formatDate(dates[i]),
						close[i],
						sf.getOutFastK()[i],
						sf.getOutFastD()[i],
						diff[i],
						bs != null ? String.valueOf(bs) : "");
				sb.append(s);
			}
		}
		
		sa.close(symbol, close[close.length-1]);
		
		return sb.toString();		
	}
	
	public static void main(String[] args) {

		
	}

}
