package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;

import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

public class MA extends IndicatorFunction {

	// indicator-based params 
	int optInTimePeriod = 12;
	int lookback = 0;
	MAType maType;
	
	double[] outReal;
	Date[] dates;
	
	public MA(int optInTimePeriod, MAType maType) {
		this.optInTimePeriod = optInTimePeriod;
		this.lookback = core.movingAverageLookback(optInTimePeriod, maType);
		this.maType = maType;
	}
	
	public int getLookback() {
		return lookback;
	}
	
	public double[] getOutReal() {
		return outReal;
	}

	public void evaluate(double[] inReal, Date[] dates) {
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		this.dates = dates;
		this.outReal = new double[inReal.length];
		switch (maType) {
		case Sma:
			core.sma(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Ema:
			core.ema(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Dema:
			core.dema(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Tema:
			core.tema(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Kama:
			core.kama(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Trima:
			core.trima(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		case Wma:
			core.wma(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
			break;
		default:
			throw new IllegalArgumentException();
		}

		normalizeArray(outReal, lookback);
	}

}
