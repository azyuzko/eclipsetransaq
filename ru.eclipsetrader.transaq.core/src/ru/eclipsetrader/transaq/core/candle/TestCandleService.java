package ru.eclipsetrader.transaq.core.candle;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import org.junit.Test;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TestCandleService extends TestCase {

	TQSymbol symbol = TQSymbol.BRV5;
	TQCandleService instance;
	TransaqServer server;
	Date fromDate = Utils.parseDate("04.08.2015 10:00:00.000");
	Date toDate = Utils.parseDate("04.08.2015 12:00:00.000");

	boolean connected = false;
	;
	
	@Override
	protected void setUp() throws Exception {
		instance = TQCandleService.getInstance();
		//server = TransaqServerManager.getInstance().connect(Constants.DEFAULT_SERVER_ID);
		//connected = server.getStatus() == ConnectionStatus.CONNECTED;
	}
	
	@Test
	public void testConvertCandleList() {
		List<Candle> list_from = instance.getSavedCandles(symbol, CandleType.CANDLE_15M, fromDate, toDate);
		list_from.forEach(c -> System.out.println(c));
		System.out.println();
		List<Candle> list_to = instance.getSavedCandles(symbol, CandleType.CANDLE_1H, fromDate, toDate);
		list_to.forEach(c -> System.out.println(c));
		System.out.println();
		List<Candle> list_converted = TQCandleService.convertCandleList(CandleType.CANDLE_15M, CandleType.CANDLE_1H, list_from); 
		list_converted.forEach(c -> System.out.println(c));
		System.out.println();
		assertEquals(list_to.size(), list_converted.size());
		for (int i=0; i < list_converted.size()-1; i++) {
			assertEquals(list_to.get(i), list_converted.get(i));
		}
	}
	
	@Test
	public void testGetHistoryData() {
		if (connected) {
			ExecutorService pool = Executors.newCachedThreadPool();
			Stream.iterate(0, i -> i+1).limit(100).forEach((i) -> pool.submit( () -> {
				Stream.of(CandleType.CANDLE_1M, CandleType.CANDLE_15M, CandleType.CANDLE_1H).forEach( (ct) -> {
						int x = (int)(Math.random()*1000);
						instance.getHistoryData(symbol, ct, x);
				});
			}) );
		}
	}

	public static void main(String[] args) {
		
		Stream.iterate(0, i -> i+1).limit(100).forEach((i) -> System.out.println());
		
	}
	
}
