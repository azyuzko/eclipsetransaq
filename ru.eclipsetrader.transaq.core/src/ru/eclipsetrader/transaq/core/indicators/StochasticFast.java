package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.account.SimpleAccount;
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

	int lookback;
	
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

	public static void simpleStochFTest(CandleList cl, SimpleAccount sa) {
		
		StochasticFast sf = new StochasticFast();
		sf.evaluate(cl);
		
		Date[] dates = cl.dates();
		
		for (int i = 0; i < dates.length-1; i++) {
			System.out.println(Utils.formatDate(dates[i]) + " - " + sf.getOutFastD()[i] + " " + sf.getOutFastK()[i]);
		}
		
	}
	
	public static void main(String[] args) {

		Date fromDate = Utils.parseDate("10.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("19.08.2015 00:00:00.000");

		CandleType candleType = CandleType.CANDLE_15M;
		List<Candle> candles = DataManager.getCandles(new TQSymbol(
				BoardType.FUT, "SiU5"), candleType, fromDate, toDate);
		CandleList cl = new CandleList(candleType);
		cl.appendCandles(candles);

		StochasticFast sf = new StochasticFast();

		sf.evaluate(cl);
		
		double[] inHigh = sf.getInHigh();
		double[] inLow = sf.getInLow();
		double[] inClose = sf.getInClose();

		Date[] dates = cl.dates();

		StringBuilder sb = new StringBuilder();
		sb.append("\n" + "dates     :"
				+ Utils.printArray(dates, "%1$2tm-%1$2td%1$6tR") + "\n");
		sb.append("high      :" + Utils.printArray(inHigh, "%11.2f") + "\n");
		sb.append("low       :" + Utils.printArray(inLow, "%11.2f") + "\n");
		sb.append("close     :" + Utils.printArray(inClose, "%11.2f") + "\n");
		sb.append("slowK     :" + Utils.printArray(sf.getOutFastK(), "%11.4f") + "\n");
		sb.append("slowD     :" + Utils.printArray(sf.getOutFastD(), "%11.4f") + "\n");
		sb.append("dates = " + dates.length + "\n");
		sb.append("outBegIdx = " + sf.getOutBegIdx() + "\n");
		sb.append("outNBElement = " + sf.getOutNBElement() + "\n");
		sb.append("lookback = " + sf.getLookback() + "\n");

		System.out.println(sb.toString());
		
		SimpleAccount sa = new SimpleAccount(100000.0);
		simpleStochFTest(cl, sa);
		
	}

}
