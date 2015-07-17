package ru.eclipsetrader.transaq.core.indicators;

import java.util.Arrays;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

public class Indicator {
	
	static Core core = new Core();
	
	String code;
	int quantity = 1000;
	int size = 0;
	
	// indicator-based params 
	int optInTimePeriod = 3;
	int lookback = core.movingAverageLookback(optInTimePeriod, MAType.Sma);
	
	private double[] outData;
	
	public Indicator(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public int getLookback() {
		return lookback;
	}

	public void evaluate(double[] inReal) {
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		double[] outReal = new double[inReal.length];
		core.sma(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
		normalizeArray(outReal, lookback);
		this.outData = outReal;
	}
	
	@Override
	public String toString() {
		return "Indicator " + getClass().getSimpleName() + " (" + hashCode() + ")";
	}
	
	/**
	 * Сдвигает массив рассчитанных значений в сторону конца на lookback
	 * @param outReal
	 * @param lookback
	 */
	public static void normalizeArray(double[] outReal, int lookback) {
		System.arraycopy(outReal, 0, outReal, lookback, outReal.length-lookback);
		Arrays.fill(outReal, 0, lookback, 0.0);
	}
	
	public static double[] createTestData() {
		double[] data = new double[150000];
		
		for (int i = 0; i < data.length; i++) {
			data[i] = Math.random();
		}
		
		/*
		data[0] = 5.3;
		data[1] = 6.7;
		data[2] = 7.9;		
		data[3] = 7.1;		
		data[4] = 5.2;		
		data[5] = 4.1;		
		data[6] = 3.5;		
		data[7] = 5.4;		
		data[8] = 7.3;		
		data[9] = 9.4;		
		data[10] = 8.0;		
		data[11] = 6.6;		
		data[12] = 7.9;		
		data[13] = 9.2;		
		data[14] = 7.6;	*/
		return data;
	}
	
	public static void main(String[] args) {

		Core core = new Core();
		double[] inReal = {56.88, 56.73, 56.81, 56.74, 57.57, 57.29, 57.59, 57.48, 58.36, 58.47, 58.14, 58.29, 58.38, 58.65};
		
		int optInTimePeriod = 2;
		
		int lookback = core.movingAverageLookback(optInTimePeriod, MAType.Ema);
		System.out.println("lookback = " + lookback);
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		double[] outReal = new double[inReal.length];
		core.ema(0, inReal.length-1, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
		System.out.println("inReal.length = " + inReal.length);
		System.out.println("outBegIdx = " + outBegIdx.value);
		System.out.println("outNBElement = " + outNBElement.value);
		System.out.println("--");
		
		for (int i = 0; i < inReal.length; i++) {
			System.out.println("" + i + " : " + inReal[i] + " = " +outReal[i]);
		}
		
		System.out.println("-- 2");

		normalizeArray(outReal, lookback);

		for (int i = 0; i < outReal.length; i++) {
			System.out.println("" + i + " : " + inReal[i] + " = " +outReal[i]);
		}
		
	}
}
