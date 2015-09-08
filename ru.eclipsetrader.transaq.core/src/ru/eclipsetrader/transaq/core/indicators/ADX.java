package ru.eclipsetrader.transaq.core.indicators;

import com.tictactec.ta.lib.MInteger;

public class ADX extends IndicatorFunction {

	int optInTimePeriod;
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	double[] outReal;
	
	public ADX(int optInTimePeriod) {
		this.optInTimePeriod = optInTimePeriod;
	}
	
	public void evaluate(double[] inHigh, double[] inLow, double[] inClose) {
		int startIdx = 0;
		int endIdx = inHigh.length-1;
		core.adx(startIdx, endIdx, inHigh, inLow, inClose, optInTimePeriod, outBegIdx, outNBElement, outReal);
		normalizeArray(outReal, lookback);
	}
	
	public int getOptInTimePeriod() {
		return optInTimePeriod;
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
}
