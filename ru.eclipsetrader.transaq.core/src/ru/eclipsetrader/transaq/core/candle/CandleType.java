package ru.eclipsetrader.transaq.core.candle;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.exception.UnimplementedException;

/**
 * ������ ����� � ��������
 * @author Zyuzko-AA
 *
 */
public enum CandleType {

	CANDLE_1M(60), 
	CANDLE_2M(120), 
	CANDLE_3M(180), 
	CANDLE_4M(240), 
	CANDLE_5M(300),
	CANDLE_6M(360),
	CANDLE_10M(600), 
	CANDLE_15M(900), 
	CANDLE_20M(1200), 
	CANDLE_30M(1800),
	CANDLE_1H(3600), 
	CANDLE_2H(7200), 
	CANDLE_3H(10800), 
	CANDLE_4H(14400),
	CANDLE_1D(86400), 
//	CANDLE_2D(172800), 
	CANDLE_1W(604800), 
	CANDLE_2W(1209600);
	
	Map<Integer, CandleType> enumStorage = new HashMap<Integer, CandleType>(); 

	int seconds;
	
	CandleType(int seconds) {
		this.seconds = seconds;
	}
	
	public int getSeconds() {
		return this.seconds;
	}
	
	
	/**
	 * ���� ������� ������ ����� 1 ����/���/������ � �.�.
	 * @return
	 */
	public int getCalendarBase() {
		switch (this) {
		case CANDLE_1M:
		case CANDLE_2M:
		case CANDLE_3M:
		case CANDLE_4M:
		case CANDLE_5M:
		case CANDLE_6M:
		case CANDLE_10M: 
		case CANDLE_15M:
		case CANDLE_20M: 
		case CANDLE_30M:
			return Calendar.HOUR;

		case CANDLE_1H:
		case CANDLE_2H: 
		case CANDLE_3H: 
		case CANDLE_4H:
			return Calendar.DATE;
		
		case CANDLE_1D:
//		case CANDLE_2D:
			return Calendar.MONTH;
			
		default:
			throw new UnimplementedException();
		}
	}
	
	public static CandleType fromSeconds(int seconds) {
		for (CandleType candleType : CandleType.class.getEnumConstants()) {
			if (candleType.getSeconds() == seconds) {
				return candleType;
			}
		}
		throw new IllegalArgumentException("CandleType not found for seconds = " + seconds);
	}
	
	public static Comparator<CandleType> comparator = new Comparator<CandleType>() {

		@Override
		public int compare(CandleType candleType1, CandleType candleType2) {
			return Integer.valueOf(candleType1.getSeconds()).compareTo(candleType2.getSeconds());
		}

	};
	
}
