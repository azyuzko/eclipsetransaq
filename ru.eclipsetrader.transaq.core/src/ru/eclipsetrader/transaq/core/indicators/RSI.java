package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.tictactec.ta.lib.MInteger;

public class RSI extends IndicatorFunction {

	int optInTimePeriod = 5;
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	double[] outReal;
	
	public RSI(int optInTimePeriod) {
		this.optInTimePeriod = optInTimePeriod;
		lookback = core.rsiLookback(optInTimePeriod);
	}
	
	public void evaluate(DoubleStream doubleStream) {
		evaluate(doubleStream.toArray());
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

	public double[] getOutReal() {
		return outReal;
	}

	public static void main(String[] args) {
		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		CandleType candleType = CandleType.CANDLE_15M;
		Date fromDate = Utils.parseDate("10.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("20.08.2015 00:00:00.000");
		
		List<Candle> candles = DataManager.getCandles(symbol, candleType, fromDate, toDate);
		CandleList cl = new CandleList(symbol, candleType);
		cl.setMaxSize(20);
		cl.appendCandles(candles);
				
		RSI rsi = new RSI(6);
		rsi.evaluate(cl.streamPrice(PriceType.CLOSE));
		Stream<Object> res = cl.stream(true).map(c -> c.getDate());
		
		System.out.println("\n"
				+ 		   "dates  :" + Utils.printArray(res, "%1$10tR"));
		System.out.println("inReal :" + Utils.printArray(cl.streamPrice(PriceType.CLOSE).toArray(), "%10.0f"));
		System.out.println("outReal:" + Utils.printArray(rsi.getOutReal(), "%10.2f"));
	}
}
