package ru.eclipsetrader.transaq.core.server;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

public class TransaqScheduleMonitor implements Runnable {
	
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	MarketSchedule marketSchedule;
	ScheduledFuture<?> scheduledFuture;
	
	static int DELAY = 5; 
	
	public TransaqScheduleMonitor(MarketSchedule marketSchedule) {
		this.marketSchedule = marketSchedule;
	}

	@Override
	public void run() {
		if (TransaqServer.getInstance().getStatus() != ConnectionStatus.CONNECTED) {
			Date date = TransaqServer.getInstance().getServerTime();
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			if (marketSchedule.canConnect(localDateTime)) {
				System.err.println("Trying to reconnect to server");
			}
		}
	}

	public void start() {
		scheduledFuture = ses.scheduleWithFixedDelay(this, DELAY, DELAY, TimeUnit.SECONDS);
	}
	
	public void stop() {
		if (scheduledFuture != null && !scheduledFuture.isDone()) {
			scheduledFuture.cancel(true);
			scheduledFuture = null;
		}
	}
	
}
