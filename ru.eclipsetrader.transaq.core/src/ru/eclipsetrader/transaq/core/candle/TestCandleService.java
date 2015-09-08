package ru.eclipsetrader.transaq.core.candle;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import org.junit.Test;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.server.TransaqServerManager;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TestCandleService extends TestCase {

	TQSymbol symbol = TQSymbol.SiU5;
	TQCandleService instance;
	ITransaqServer server;
	Date fromDate = Utils.parseDate("03.08.2015 10:30:00.000");
	Date toDate = Utils.parseDate("06.08.2015 10:40:00.000");

	boolean connected = false;
	;
	
	@Override
	protected void setUp() throws Exception {
		instance = TQCandleService.getInstance();
		server = TransaqServerManager.getInstance().connect(Constants.DEFAULT_SERVER_ID);
		connected = server.getStatus() == ConnectionStatus.CONNECTED;
	}
	
	@Test
	public void testConvertCandleList() {
		List<Candle> list_1M = instance.getSavedCandles(symbol, CandleType.CANDLE_1M, fromDate, toDate);
		List<Candle> list_10M = instance.getSavedCandles(symbol, CandleType.CANDLE_10M, fromDate, toDate);
		List<Candle> list_converted = TQCandleService.convertCandleList(CandleType.CANDLE_1M, CandleType.CANDLE_10M, list_1M); 
		assertEquals(list_10M.size(), list_converted.size());
		for (int i=0; i < list_converted.size()-1; i++) {
			assertEquals(list_10M.get(i), list_converted.get(i));
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
