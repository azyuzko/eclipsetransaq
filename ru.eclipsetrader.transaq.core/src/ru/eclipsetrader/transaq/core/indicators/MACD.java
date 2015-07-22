package ru.eclipsetrader.transaq.core.indicators;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import com.tictactec.ta.lib.MInteger;

public class MACD  extends IndicatorFunction {

	int lookback;
	int optInFastPeriod;
	int optInSlowPeriod;
	int optInSignalPeriod;
	private double[] outMACD;
	private double[] outMACDSignal;
	private double[] outMACDHist;
	private MInteger outBegIdx = new MInteger();
	private MInteger outNBElement = new MInteger();
	private Date[] dates;

	public MACD(int optInFastPeriod, int optInSlowPeriod, int optInSignalPeriod) {
		this.optInFastPeriod = optInFastPeriod;
		this.optInSlowPeriod = optInSlowPeriod;
		this.optInSignalPeriod = optInSignalPeriod;
		this.lookback = core.macdLookback(optInFastPeriod, optInSlowPeriod, optInSignalPeriod);
	}

	public void evaluate(double[] inReal, Date[] dates) {
		this.dates = dates;
		outMACD = new double[inReal.length];
		outMACDSignal = new double[inReal.length];
		outMACDHist = new double[inReal.length];
		core.macd(0, inReal.length-1, inReal, optInFastPeriod, optInSlowPeriod, optInSignalPeriod, outBegIdx, outNBElement, outMACD, outMACDSignal, outMACDHist);
		normalizeArray(outMACD, lookback);
		normalizeArray(outMACDSignal, lookback);
		normalizeArray(outMACDHist, lookback);
	}

	public int getOptInFastPeriod() {
		return optInFastPeriod;
	}

	public int getOptInSlowPeriod() {
		return optInSlowPeriod;
	}

	public int getOptInSignalPeriod() {
		return optInSignalPeriod;
	}

	public int getLookback() {
		return lookback;
	}
	
	public double[] getOutMACD() {
		return outMACD;
	}

	public double[] getOutMACDSignal() {
		return outMACDSignal;
	}

	public double[] getOutMACDHist() {
		return outMACDHist;
	}
	
	public double[] getOutMACD(int lastCount) {
		return ArrayUtils.subarray(outMACD, outMACD.length-lastCount, outMACD.length);
	}

	public double[] getOutMACDSignal(int lastCount) {
		return ArrayUtils.subarray(outMACDSignal, outMACDSignal.length-lastCount, outMACDSignal.length);
	}

	public double[] getOutMACDHist(int lastCount) {
		return ArrayUtils.subarray(outMACDHist, outMACDHist.length-lastCount, outMACDHist.length);
	}

	public double getOutMACDValue(Date onDate) {
		return outMACD[ArrayUtils.indexOf(dates, onDate)]; 
	}

	public double getOutMACDSignalValue(Date onDate) {
		return outMACDSignal[ArrayUtils.indexOf(dates, onDate)];
	}

	public double getOutMACDHistValue(Date onDate) {
		return outMACDHist[ArrayUtils.indexOf(dates, onDate)];
	}
	
	public MInteger getOutBegIdx() {
		return outBegIdx;
	}

	public MInteger getOutNBElement() {
		return outNBElement;
	}

	public Date[] getDates() {
		return dates;
	}
	
	public Date[] getDates(int lastCount) {
		return ArrayUtils.subarray(dates, dates.length-lastCount, dates.length);
	}
	
}
