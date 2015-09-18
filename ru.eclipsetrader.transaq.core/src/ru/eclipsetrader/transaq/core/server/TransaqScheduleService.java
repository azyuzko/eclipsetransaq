package ru.eclipsetrader.transaq.core.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.exception.ConnectionException;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.schedule.DaemonThreadFactory;
import ru.eclipsetrader.transaq.core.util.MailUtils;

public class TransaqScheduleService implements Runnable {
	
	Logger logger = LogManager.getLogger("TransaqScheduleService");
	
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(5, new DaemonThreadFactory());
	
	ScheduledFuture<?> scheduledFuture;
	
	static int DEFAULT_DELAY = 5; 
	static int MAX_FAIL_CONNECTIONS = 10; 
	volatile int failedConnectionCount = 0;
	
	@Override
	public void run() {
		TransaqServer instance = TransaqServer.getInstance();
		MarketSchedule marketSchedule = instance.getMarketSchedule();
		ConnectionStatus status = instance.getStatus();
		logger.debug("Check scheduled transaq server status = " + status);
		Date date = instance.getServerTime();
		LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (status == ConnectionStatus.DISCONNECTED) {
			if (marketSchedule.canConnect(localDateTime)) {
				logger.info("Trying to connect to transaq server, attepmt = " + failedConnectionCount);
				try {
					instance.connect((c) -> {});
					if (instance.getStatus() == ConnectionStatus.CONNECTED) {
						failedConnectionCount = 0;
						logger.info("Connect complete");
					} else {
						logger.warn("Connection NOT complete, received status = " + instance.getStatus());
						failedConnectionCount++;
					}
				} catch (ConnectionException e) {
					e.printStackTrace();
					failedConnectionCount++;
				}
				if (failedConnectionCount > MAX_FAIL_CONNECTIONS) {
					logger.warn("Schedule transaq service stop cause max connect attempts reached");
					stop();
					MailUtils.sendMail(Settings.MAIL_NOTIFICATION_ADDRESS, "TransaqScheduleService notification", "Schedule transaq service stop cause max connect attempts reached");
				}
			}
		} else if (status == ConnectionStatus.CONNECTED) {
			if (marketSchedule.canConnect(localDateTime)) {
				// Обновим статус
				instance.updateStatus();
			} else {
				// надо отсоединиться
				instance.disconnect();
			}
		}
	}
	
	public void start(){
		start(DEFAULT_DELAY);
	}

	public void start(int delay) {
		scheduledFuture = ses.scheduleWithFixedDelay(this, delay, delay, TimeUnit.SECONDS);
		logger.info("Schedule transaq service started with delay = " + delay + " " + TimeUnit.SECONDS);
	}
	
	public void stop() {
		logger.debug("Stop called..");
		if (scheduledFuture != null && !scheduledFuture.isDone()) {
			boolean cancelled = scheduledFuture.cancel(true);
			if (cancelled) {
				scheduledFuture = null;
				logger.info("Schedule transaq service cancelled");
			} else {
				logger.warn("Schedule transaq service NOT cancelled!");
			}
		}
	}

	public static void main (String[] args) throws IOException {
		TransaqScheduleService tss = new TransaqScheduleService();
		tss.start();
		System.in.read();
		tss.stop();
		System.in.read();
		tss.start();
		System.in.read();
		tss.stop();
	}
}
