package ru.eclipsetrader.transaq.core.candle;

import junit.framework.TestCase;

import org.junit.Test;

public class TestCandleType extends TestCase {
	
	@Test
	public void testClosest60Candle() {
		assertEquals(CandleType.CANDLE_15S.getClosest60Candle(), CandleType.CANDLE_15S);
		assertEquals(CandleType.CANDLE_1M.getClosest60Candle(), CandleType.CANDLE_1M);
		assertEquals(CandleType.CANDLE_61S.getClosest60Candle(), CandleType.CANDLE_1M);
		assertEquals(CandleType.CANDLE_62S.getClosest60Candle(), CandleType.CANDLE_1M);
		assertEquals(CandleType.CANDLE_5M.getClosest60Candle(), CandleType.CANDLE_5M);
	}
	
	@Test
	public void testFromSeconds() {
		assertEquals(CandleType.fromSeconds(60), CandleType.CANDLE_1M);
		assertEquals(CandleType.fromSeconds(61), CandleType.CANDLE_61S);
		assertEquals(CandleType.fromSeconds(1), CandleType.CANDLE_1S);
		assertEquals(CandleType.fromSeconds(60 * 60), CandleType.CANDLE_1H);
	}
}
