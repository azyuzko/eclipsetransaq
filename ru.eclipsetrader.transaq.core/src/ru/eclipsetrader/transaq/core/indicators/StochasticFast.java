package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
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

		inHigh = cl.streamPrice(PriceType.HIGH).toArray();
		inLow = cl.streamPrice(PriceType.LOW).toArray();
		inClose = cl.streamPrice(PriceType.CLOSE).toArray();

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
	
	public static void main(String[] args) {
		Date fromDate = Utils.parseDate("10.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("19.08.2015 00:00:00.000");

		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		CandleType candleType = CandleType.CANDLE_15M;
		List<Candle> candles = DataManager.getCandles(symbol, candleType, fromDate, toDate);
		CandleList cl = new CandleList(symbol, candleType);
		cl.appendCandles(candles);
		
		StochasticFast sf = new StochasticFast();
		sf.evaluate(cl);
		
		System.out.println(sf);
		
	}

}
