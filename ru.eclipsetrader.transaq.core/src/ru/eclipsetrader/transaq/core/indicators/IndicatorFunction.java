package ru.eclipsetrader.transaq.core.indicators;

import java.util.Arrays;

import ru.eclipsetrader.transaq.core.util.Utils;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

public abstract class IndicatorFunction {
	
	static Core core = new Core();
	
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
		if (outReal != null && outReal.length > lookback) {
			System.arraycopy(outReal, 0, outReal, lookback, outReal.length-lookback);
			Arrays.fill(outReal, 0, lookback, 0);
		}
	}
	
	public static double[] createTestData() {
		double[] data = new double[15];
		
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
		data[14] = 7.6;	
		return data;
	}
	
	public static void main(String[] args) {
		double[] data = new double[] {1.0, 2.0, 3.0, 4.0};
		int lookback = 2;
		System.out.println(Utils.printArray(data));
		normalizeArray(data, lookback);
		System.out.println(Utils.printArray(data));
	}
	
	public static void main1(String[] args) {

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
