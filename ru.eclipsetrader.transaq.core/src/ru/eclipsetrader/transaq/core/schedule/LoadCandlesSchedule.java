package ru.eclipsetrader.transaq.core.schedule;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.server.TransaqServer;

public class LoadCandlesSchedule {
	
	static Logger logger = LogManager.getLogger(LoadCandlesSchedule.class);
	
	static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

	public static void scheduleLoadCandles() {
		logger.info("Scheduling loadcandles service");
		for (TQSymbol symbol : TQSymbol.workingSymbolSet()) {
			for (CandleType candleType : TQCandleService.getInstance().getCandleTypes()) {
				int count = 100; // по умолчанию 100 
				switch (candleType) {
				case CANDLE_1M:		count = 60 * 18; break;
				case CANDLE_15M:	count = 4 * 18 * 5; break;
				case CANDLE_1H:		count = 18 * 5; break;
				case CANDLE_1D:		count = 30; break;
				case CANDLE_1W:		count = 30; break;
				default:
					break;
				}
				int delay = candleType.getSeconds() * count / 2; 
				executorService.scheduleWithFixedDelay(new LoadCandlesRunnable(symbol, candleType, count), 0, delay, TimeUnit.SECONDS);
			}
		}
		logger.info("Scheduled successfully");		
	}
	
	static class LoadCandlesRunnable implements Runnable {
		TQSymbol symbol;
		CandleType candleType;
		int count;
		
		public LoadCandlesRunnable(TQSymbol symbol, CandleType candleType, int count) {
			this.symbol = symbol;
			this.candleType = candleType;
			this.count = count;
		}
		
		@Override
		public void run() {
			logger.debug("Running scheduled thread for load candles " + symbol + " " + candleType + " count = " + count);
			try {
				if (TransaqServer.getInstance() != null && TransaqServer.getInstance().getStatus() == ConnectionStatus.CONNECTED) {
					List<Candle> candles = TQCandleService.getInstance().getHistoryData(symbol, candleType, count, true);	// , // по умолчанию за день
					logger.debug("Received " + candles.size() + " candles");				
					TQCandleService.getInstance().persist(symbol, candleType, candles);
					logger.debug("Completed.");
				} else {
					logger.warn("No connection to server. LoadCandles will not be performed.");
				}
			} catch (Exception e) {
				logger.throwing(e);
			}
		}
		
	}

}
