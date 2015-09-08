package ru.eclipsetrader.transaq.core.candle;

import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TestCandle extends TestCase {

	Candle candle;
	Tick tick1;
	Tick tick2;
	Tick tick3;
	
	@Override
	protected void setUp() throws Exception {
		candle = new Candle();
		tick1 = new TickTrade().setQuantity(5).setPrice(10.0).setTime(Utils.parseDate("01.09.2015 12:15:32.010"));
		tick2 = new TickTrade().setQuantity(10).setPrice(8.5).setTime(Utils.parseDate("01.09.2015 12:16:22.010"));
		tick3 = new TickTrade().setQuantity(2).setPrice(11.0).setTime(Utils.parseDate("01.09.2015 12:16:30.210"));
	}
	
	@Test
	public void testProcessTick() {
		candle.setDate(Utils.parseDate("01.09.2015 12:15:00.000"));
		candle.processTick(tick1);
		Arrays.asList(PriceType.OPEN, PriceType.LOW, PriceType.HIGH, PriceType.CLOSE, PriceType.WEIGHTED_CLOSE)
			.forEach(pt -> assertTrue(candle.getPriceValueByType(pt) == tick1.getPrice()));
		candle.processTick(tick2);
		assertEquals(candle.getPriceValueByType(PriceType.OPEN), tick1.getPrice());
		assertEquals(candle.getPriceValueByType(PriceType.HIGH), Math.max(tick1.getPrice(), tick2.getPrice()));
		assertEquals(candle.getPriceValueByType(PriceType.LOW), Math.min(tick1.getPrice(), tick2.getPrice()));
		assertEquals(candle.getPriceValueByType(PriceType.CLOSE), tick2.getPrice());
		candle.processTick(tick3);
		assertEquals(candle.getPriceValueByType(PriceType.WEIGHTED_CLOSE), 10.375, 0.001); // 
		assertEquals(candle.getPriceValueByType(PriceType.VOLUME_WEIGHTED), 9.235294117647058, 0.001); // 
	}
	
	@Test
	public void testEquals() {
		Candle c1 = new Candle();
		Candle c2 = new Candle();
		long time = System.currentTimeMillis();
		Date date1 = new Date(time);
		Date date2 = new Date(time);
		c1.setDate(date1).setClose(10.0).setOpen(5.0).setHigh(15.0).setLow(2.0).setVolume(30);
		c2.setDate(date2).setClose(10.0).setOpen(5.0).setHigh(15.0).setLow(2.0).setVolume(30);
		assertTrue(c1.equals(c2));
		c1.setDate(new Date(time + 1));
		assertFalse(c1.equals(c2));
	}

}
