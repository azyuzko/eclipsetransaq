package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;
import java.util.List;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

public class RSI extends IndicatorFunction {

	int lookback;
	int optInTimePeriod = 5;
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	double[] outReal;
	
	public RSI(int optInTimePeriod) {
		this.optInTimePeriod = optInTimePeriod;
		lookback = core.rsiLookback(optInTimePeriod);
	}
	
	public void evaluate(double[] inReal) {
		outReal = new double[inReal.length];
		int startIdx = 0;
		int endIdx = inReal.length-1;
		core.rsi(startIdx, endIdx, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
		normalizeArray(outReal, lookback);
	}

	public int getOptInTimePeriod() {
		return optInTimePeriod;
	}

	public void setOptInTimePeriod(int optInTimePeriod) {
		this.optInTimePeriod = optInTimePeriod;
		this.lookback = core.rsiLookback(optInTimePeriod);
	}

	public int getLookback() {
		return lookback;
	}

	public Integer getOutBegIdx() {
		if (outBegIdx != null) {
			return outBegIdx.value;
		} else {
			return null;
		}
	}

	public MInteger getOutNBElement() {
		return outNBElement;
	}

	public double[] getOutReal() {
		return outReal;
	}

	public static void main(String[] args) {
		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		CandleType candleType = CandleType.CANDLE_15M;
		PriceType priceType = PriceType.CLOSE;
		Date fromDate = Utils.parseDate("10.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("20.08.2015 00:00:00.000");
		
		List<Candle> candles = DataManager.getCandles(symbol, candleType, fromDate, toDate);
		CandleList cl = new CandleList(symbol, candleType);
		cl.appendCandles(candles);
		double[] inReal = cl.values(priceType).getSecond();
		Date[] dates = cl.values(priceType).getFirst();
		
		RSI rsi = new RSI(10);
		rsi.evaluate(inReal);
		
		System.out.println("\n"
				+ 		   "dates  :" + Utils.printArray(dates, "%1$10tR"));
		System.out.println("inReal :" + Utils.printArray(inReal, "%10.0f"));
		System.out.println("outReal:" + Utils.printArray(rsi.getOutReal(), "%10.2f"));
	}
}
