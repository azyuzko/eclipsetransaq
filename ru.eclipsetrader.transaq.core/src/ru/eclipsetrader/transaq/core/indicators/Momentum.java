package ru.eclipsetrader.transaq.core.indicators;

import java.util.stream.DoubleStream;

import com.tictactec.ta.lib.MInteger;

public class Momentum  extends IndicatorFunction {
	
	int optInTimePeriod;
	
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	double[] outReal;
	
	public Momentum(int optInTimePeriod) {
		this.optInTimePeriod = optInTimePeriod;
		this.lookback = core.momLookback(optInTimePeriod);
	}
	
	public void evaluate(DoubleStream doubleStream) {
		evaluate(doubleStream.toArray());
	}
	
	public void evaluate(double[] inReal) {
		outReal = new double[inReal.length];
		int startIdx = 0;
		int endIdx = inReal.length-1;
		core.mom(startIdx, endIdx, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
		normalizeArray(outReal, lookback);
	}
	
	public int getOptInTimePeriod() {
		return optInTimePeriod;
	}

	public double[] getOutReal() {
		return outReal;
	}

	

}
