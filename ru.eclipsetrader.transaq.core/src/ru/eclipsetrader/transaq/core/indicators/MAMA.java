package ru.eclipsetrader.transaq.core.indicators;

import java.util.stream.DoubleStream;

import com.tictactec.ta.lib.MInteger;

public class MAMA extends IndicatorFunction {

	double optInFastLimit, optInSlowLimit;
	double[] outMAMA;
	double[] outFAMA;
	
	public MAMA(double optInFastLimit, double optInSlowLimit) {
		this.optInFastLimit = optInFastLimit;
		this.optInSlowLimit = optInSlowLimit;
		this.lookback = core.mamaLookback(optInFastLimit, optInSlowLimit);
	}
	
	public void evaluate(DoubleStream doubleStream) {
		evaluate(doubleStream.toArray());
	}
	
	public void evaluate(double[] inReal) {
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		this.outMAMA = new double[inReal.length];
		this.outFAMA = new double[inReal.length];
		core.mama(0, inReal.length-1, inReal, optInFastLimit, optInSlowLimit, outBegIdx, outNBElement, outMAMA, outFAMA);
		normalizeArray(outMAMA, lookback);
		normalizeArray(outFAMA, lookback);
	}

	public double[] getOutMAMA() {
		return outMAMA;
	}

	public double[] getOutFAMA() {
		return outFAMA;
	}
	
	
}
