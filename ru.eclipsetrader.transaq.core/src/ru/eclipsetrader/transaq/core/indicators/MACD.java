package ru.eclipsetrader.transaq.core.indicators;

import java.util.stream.DoubleStream;

import org.apache.commons.lang3.ArrayUtils;

import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

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

	public MACD(int optInFastPeriod, int optInSlowPeriod, int optInSignalPeriod) {
		this.optInFastPeriod = optInFastPeriod;
		this.optInSlowPeriod = optInSlowPeriod;
		this.optInSignalPeriod = optInSignalPeriod;
		this.lookback = core.macdLookback(optInFastPeriod, optInSlowPeriod, optInSignalPeriod);
	}

	public RetCode macd( int startIdx,
	      int endIdx,
	      double inReal[],
	      int optInFastPeriod,
	      int optInSlowPeriod,
	      int optInSignalPeriod,
	      MInteger outBegIdx,
	      MInteger outNBElement,
	      double outMACD[],
	      double outMACDSignal[],
	      double outMACDHist[],
	      MAType maType)
	   {
	      if( startIdx < 0 )
	         return RetCode.OutOfRangeStartIndex ;
	      if( (endIdx < 0) || (endIdx < startIdx))
	         return RetCode.OutOfRangeEndIndex ;
	      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
	         optInFastPeriod = 12;
	      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
	         return RetCode.BadParam ;
	      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
	         optInSlowPeriod = 26;
	      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
	         return RetCode.BadParam ;
	      if( (int)optInSignalPeriod == ( Integer.MIN_VALUE ) )
	         optInSignalPeriod = 9;
	      else if( ((int)optInSignalPeriod < 1) || ((int)optInSignalPeriod > 100000) )
	         return RetCode.BadParam ;
	      return TA_INT_MACD ( startIdx, endIdx, inReal,
	         optInFastPeriod,
	         optInSlowPeriod,
	         optInSignalPeriod,
	         outBegIdx,
	         outNBElement,
	         outMACD,
	         outMACDSignal,
	         outMACDHist,
	         maType);
	   }
	   RetCode TA_INT_MACD( int startIdx,
	      int endIdx,
	      double inReal[],
	      int optInFastPeriod,
	      int optInSlowPeriod,
	      int optInSignalPeriod_2,
	      MInteger outBegIdx,
	      MInteger outNBElement,
	      double outMACD[],
	      double outMACDSignal[],
	      double outMACDHist[],
	      MAType maType)
	   {
	      double []slowEMABuffer ;
	      double []fastEMABuffer ;
	      double k1, k2;
	      RetCode retCode;
	      int tempInteger;
	      MInteger outBegIdx1 = new MInteger() ;
	      MInteger outNbElement1 = new MInteger() ;
	      MInteger outBegIdx2 = new MInteger() ;
	      MInteger outNbElement2 = new MInteger() ;
	      int lookbackTotal, lookbackSignal;
	      int i;
	      if( optInSlowPeriod < optInFastPeriod )
	      {
	         tempInteger = optInSlowPeriod;
	         optInSlowPeriod = optInFastPeriod;
	         optInFastPeriod = tempInteger;
	      }
	      if( optInSlowPeriod != 0 )
	         k1 = ((double)2.0 / ((double)(optInSlowPeriod + 1))) ;
	      else
	      {
	         optInSlowPeriod = 26;
	         k1 = (double)0.075;
	      }
	      if( optInFastPeriod != 0 )
	         k2 = ((double)2.0 / ((double)(optInFastPeriod + 1))) ;
	      else
	      {
	         optInFastPeriod = 12;
	         k2 = (double)0.15;
	      }
	      lookbackSignal = core.movingAverageLookback ( optInSignalPeriod_2, maType);
	      lookbackTotal = lookbackSignal;
	      lookbackTotal += core.movingAverageLookback ( optInSlowPeriod, maType );
	      if( startIdx < lookbackTotal )
	         startIdx = lookbackTotal;
	      if( startIdx > endIdx )
	      {
	         outBegIdx.value = 0 ;
	         outNBElement.value = 0 ;
	         return RetCode.Success ;
	      }
	      tempInteger = (endIdx-startIdx)+1+lookbackSignal;
	      fastEMABuffer = new double[tempInteger] ;
	      slowEMABuffer = new double[tempInteger] ;
	      tempInteger = startIdx-lookbackSignal;
	      if (maType == MAType.Mama) {
	    	  retCode = core.mama( tempInteger, endIdx,
				         inReal, 0.5, 0.05,
				         outBegIdx1 , outNbElement1, fastEMABuffer, slowEMABuffer );
	      } else {
		      if (maType == MAType.Ema) { // backward compatibility
			      retCode = core.TA_INT_EMA ( tempInteger, endIdx,
			         inReal, optInSlowPeriod, k1,
			         outBegIdx1 , outNbElement1 , slowEMABuffer );
		      } else {
		    	  retCode = core.movingAverage( tempInteger, endIdx,
					         inReal, optInSlowPeriod, maType,
					         outBegIdx1 , outNbElement1 , slowEMABuffer );
		      }
		      if( retCode != RetCode.Success )
		      {
		         outBegIdx.value = 0 ;
		         outNBElement.value = 0 ;
		         return retCode;
		      }
		      if (maType == MAType.Ema) { // backward compatibility
			      retCode = core.TA_INT_EMA ( tempInteger, endIdx,
			         inReal, optInFastPeriod, k2,
			         outBegIdx2 , outNbElement2 , fastEMABuffer );
		      } else {
			      retCode = core.movingAverage( tempInteger, endIdx,
					         inReal, optInFastPeriod, maType,
					         outBegIdx2 , outNbElement2 , fastEMABuffer );
		      }
		      if( retCode != RetCode.Success )
		      {
		         outBegIdx.value = 0 ;
		         outNBElement.value = 0 ;
		         return retCode;
		      }
		      if( ( outBegIdx1.value != tempInteger) ||
		         ( outBegIdx2.value != tempInteger) ||
		         ( outNbElement1.value != outNbElement2.value ) ||
		         ( outNbElement1.value != (endIdx-startIdx)+1+lookbackSignal) )
		      {
		         outBegIdx.value = 0 ;
		         outNBElement.value = 0 ;
		         return (RetCode.InternalError) ;
		      }
	      }
	      
	      for( i=0; i < outNbElement1.value ; i++ )
	         fastEMABuffer[i] = fastEMABuffer[i] - slowEMABuffer[i];
	      System.arraycopy(fastEMABuffer,lookbackSignal,outMACD,0,(endIdx-startIdx)+1) ;
	      retCode = core.TA_INT_EMA ( 0, outNbElement1.value -1,
	         fastEMABuffer, optInSignalPeriod_2, ((double)2.0 / ((double)(optInSignalPeriod_2 + 1))) ,
	         outBegIdx2 , outNbElement2 , outMACDSignal );
	      if( retCode != RetCode.Success )
	      {
	         outBegIdx.value = 0 ;
	         outNBElement.value = 0 ;
	         return retCode;
	      }
	      for( i=0; i < outNbElement2.value ; i++ )
	         outMACDHist[i] = outMACD[i]-outMACDSignal[i];
	      outBegIdx.value = startIdx;
	      outNBElement.value = outNbElement2.value ;
	      return RetCode.Success ;
	   }
	
	public void evaluate(DoubleStream doubleStream, MAType maType) {
		evaluate(doubleStream.toArray(), maType);
	}
			
	public void evaluate(double[] inReal, MAType maType) {
		outMACD = new double[inReal.length];
		outMACDSignal = new double[inReal.length];
		outMACDHist = new double[inReal.length];
		macd(0, inReal.length-1, inReal, optInFastPeriod, optInSlowPeriod, optInSignalPeriod, outBegIdx, outNBElement, outMACD, outMACDSignal, outMACDHist, maType);
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
	
	public MInteger getOutBegIdx() {
		return outBegIdx;
	}

	public MInteger getOutNBElement() {
		return outNBElement;
	}

}
