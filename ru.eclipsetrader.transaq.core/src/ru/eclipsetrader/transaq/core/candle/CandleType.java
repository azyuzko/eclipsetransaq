package ru.eclipsetrader.transaq.core.candle;

import java.util.Calendar;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.exception.UnimplementedException;

/**
 * Размер свечи в секундах
 * @author Zyuzko-AA
 *
 */
public enum CandleType {

	CANDLE_1S(1),
	CANDLE_2S(2),
	CANDLE_3S(3),
	CANDLE_4S(4),
	CANDLE_5S(5),
	CANDLE_6S(6),
	CANDLE_7S(7),
	CANDLE_8S(8),
	CANDLE_9S(9),
	CANDLE_10S(10),
	CANDLE_15S(15),
	CANDLE_20S(20),
	CANDLE_30S(30),
	CANDLE_11S(11),
	CANDLE_12S(12),
	CANDLE_16S(16),
	CANDLE_17S(17),
	CANDLE_21S(21),
	CANDLE_22S(22),
	CANDLE_31S(31),
	CANDLE_32S(32),
	CANDLE_61S(61),
	CANDLE_62S(62),
	
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
	CANDLE_1D(86400), 
	CANDLE_1W(604800);
	
	static TreeMap<Integer, CandleType> enumStorage = new TreeMap<Integer, CandleType>();
	static {
		for (CandleType ct : CandleType.values()) {
			enumStorage.put(ct.seconds, ct);
		}
	}

	int seconds;
	
	CandleType(int seconds) {
		this.seconds = seconds;
	}
	
	public int getSeconds() {
		return this.seconds;
	}
	
	/**
	 * Возвращает ближайший меньший (к текущему) размер свечи, кратный 60/30/20/15 (минутам/секундам)
	 * Т.е. для 11S вернет 10S
	 * Для 35S вернет 30S
	 * 
	 */
	public CandleType getClosest60Candle() {
		NavigableMap<Integer, CandleType> tail = enumStorage.headMap(getSeconds(), true);
		for (Integer seconds : tail.descendingKeySet()) {
			if (seconds % 60 == 0 
				|| seconds % 30 == 0
				|| seconds % 20 == 0
				|| seconds % 15 == 0
				) {
				return enumStorage.get(seconds);
			}
		}
		return this;
	}
	
	/**
	 * Получает базовый размер свечи 1 день/час/минута и т.п.
	 * @return Calendar.MINUTE, Calendar.HOUR и т.п.
	 */
	public int getCalendarBase() {
		switch (this) {
		case CANDLE_1S:
		case CANDLE_2S:
		case CANDLE_3S:
		case CANDLE_4S:
		case CANDLE_5S:
		case CANDLE_6S:
		case CANDLE_7S:
		case CANDLE_8S:
		case CANDLE_9S:
		case CANDLE_10S:
		case CANDLE_11S:
		case CANDLE_15S:
		case CANDLE_20S:
		case CANDLE_30S:
		case CANDLE_16S:
		case CANDLE_21S:
		case CANDLE_31S:
		case CANDLE_61S:
			return Calendar.MINUTE;
		
		
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
