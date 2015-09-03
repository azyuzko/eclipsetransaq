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

public class Stochastic extends IndicatorFunction {

	int lookback;
	
	int optInFastK_Period = 6;
	int optInSlowK_Period = 14;
	MAType optInSlowK_MAType = MAType.Ema; // основная
	int optInSlowD_Period = 3; // дополнительная
	MAType optInSlowD_MAType = MAType.Sma;

	double[] inHigh;
	double[] inLow;
	double[] inClose;
	
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	
	double[] outSlowK;
	double[] outSlowD;

	public Stochastic() {
		lookback = core.stochLookback(optInFastK_Period, optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period, optInSlowD_MAType);
	}
	
	public Stochastic(int optInFastK_Period, int optInSlowK_Period, MAType optInSlowK_MAType, int optInSlowD_Period, MAType optInSlowD_MAType) {
		this.optInFastK_Period = optInFastK_Period;
		this.optInSlowK_Period = optInSlowK_Period;
		this.optInSlowK_MAType = optInSlowK_MAType;
		this.optInSlowD_Period = optInSlowD_Period;
		this.optInSlowD_MAType = optInSlowD_MAType;
		lookback = core.stochLookback(optInFastK_Period, optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period, optInSlowD_MAType);
	}

	public void evaluate(CandleList cl) {
		
		
		int startIdx = 0;
		int endIdx = cl.size() - 1;

		inHigh = cl.values(PriceType.HIGH).getSecond();
		inLow = cl.values(PriceType.LOW).getSecond();
		inClose = cl.values(PriceType.CLOSE).getSecond();

		outSlowK = new double[cl.size()];
		outSlowD = new double[cl.size()];
		core.stoch(startIdx, endIdx, inHigh, inLow, inClose, optInFastK_Period,
				optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period,
				optInSlowD_MAType, outBegIdx, outNBElement, outSlowK, outSlowD);
		normalizeArray(outSlowD, lookback);
		normalizeArray(outSlowK, lookback);
	}

	public int getLookback() {
		return lookback;
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

	public int getOptInSlowK_Period() {
		return optInSlowK_Period;
	}

	public void setOptInSlowK_Period(int optInSlowK_Period) {
		this.optInSlowK_Period = optInSlowK_Period;
	}

	public MAType getOptInSlowK_MAType() {
		return optInSlowK_MAType;
	}

	public void setOptInSlowK_MAType(MAType optInSlowK_MAType) {
		this.optInSlowK_MAType = optInSlowK_MAType;
	}

	public int getOptInSlowD_Period() {
		return optInSlowD_Period;
	}

	public void setOptInSlowD_Period(int optInSlowD_Period) {
		this.optInSlowD_Period = optInSlowD_Period;
	}

	public MAType getOptInSlowD_MAType() {
		return optInSlowD_MAType;
	}

	public void setOptInSlowD_MAType(MAType optInSlowD_MAType) {
		this.optInSlowD_MAType = optInSlowD_MAType;
	}

	public double[] getOutSlowK() {
		return outSlowK;
	}

	public double[] getOutSlowD() {
		return outSlowD;
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

	public static void main(String[] args) {

		Date fromDate = Utils.parseDate("10.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("19.08.2015 00:00:00.000");

		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		CandleType candleType = CandleType.CANDLE_15M;
		List<Candle> candles = DataManager.getCandles(symbol, candleType, fromDate, toDate);
		CandleList cl = new CandleList(symbol, candleType);
		cl.appendCandles(candles);

		Stochastic s = new Stochastic();
	
		s.evaluate(cl);
		
		double[] inHigh = s.getInHigh();
		double[] inLow = s.getInLow();
		double[] inClose = s.getInClose();

		Date[] dates = cl.dates();

		StringBuilder sb = new StringBuilder();
		sb.append("\n" + "dates     :"
				+ Utils.printArray(dates, "%1$2tm-%1$2td%1$6tR") + "\n");
		sb.append("high      :" + Utils.printArray(inHigh, "%11.2f") + "\n");
		sb.append("low       :" + Utils.printArray(inLow, "%11.2f") + "\n");
		sb.append("close     :" + Utils.printArray(inClose, "%11.2f") + "\n");
		sb.append("slowK     :" + Utils.printArray(s.getOutSlowK(), "%11.4f") + "\n");
		sb.append("slowD     :" + Utils.printArray(s.getOutSlowD(), "%11.4f") + "\n");
		sb.append("dates = " + dates.length + "\n");
		sb.append("outBegIdx = " + s.getOutBegIdx() + "\n");
		sb.append("outNBElement = " + s.getOutNBElement() + "\n");
		sb.append("lookback = " + s.getLookback() + "\n");

		System.out.println(sb.toString());
	}

}
