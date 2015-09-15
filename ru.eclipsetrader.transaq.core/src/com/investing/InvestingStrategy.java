package com.investing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.server.TransaqServer;

public class InvestingStrategy implements Runnable {
	
	static Logger logger = LogManager.getLogger("InvestingStrategy");
	static ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
	
	List<InvestingSecurity> list = new ArrayList<>();
	
	boolean paused = false;

	public InvestingStrategy() {
		//list.add( new InvestingSecurity(TQSymbol.SiZ5, InvestingSymbol.USDRUB, 1, 900));
		//list.add( new InvestingSecurity(TQSymbol.EuZ5, InvestingSymbol.EURRUB, 1, 1800));
		list.add( new InvestingSecurity(TQSymbol.BRV5, InvestingSymbol.BRENT, 1, 900));
		//list.add( new InvestingSecurity(TQSymbol.RIZ5, InvestingSymbol.RTS, 1, 900));
		
		TransaqServer.onConnectEstablished.addObserver(onConnected);
		TransaqServer.onDisconnected.addObserver(onDisconnected);
	}
	
	Observer<TransaqServer> onConnected = (t) -> {
		paused = false;
		logger.info("InvestingStrategy resuming...");
	};
	
	Observer<TransaqServer> onDisconnected = (t) -> {
		paused = true;
		logger.info("InvestingStrategy paused...");
	};

	
	public void start(){
		logger.info("Starting HelpRequestData service...");
		ses.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
		list.stream().forEach(s -> s.onStart());
	}
	
	public InvestingSecurity getInvesingSecurity(InvestingSymbol investingSymbol) {
		return list.stream().filter(l -> l.getInvestingSymbol().equals(investingSymbol)).findFirst().get();
	}

	@Override
	public void run() {
		logger.debug("Running HelpRequestData call...");
		try {
			Map<Integer, List<InvestingSecurity>> periodMap = list.stream().collect(Collectors.groupingBy(s -> Integer.valueOf(s.getRequestPeriod())));

			for (Integer period : periodMap.keySet()) {
				String callResult = InvestingUtils.callRest(periodMap.get(period).stream().map(is -> is.getInvestingSymbol()).collect(Collectors.toList()), period);
				Map<InvestingSymbol, InvestingRequest> result = InvestingUtils.parse(callResult);
				result.forEach((a,b) -> getInvesingSecurity(a).processRequest(b));
			}
			logger.debug("Completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		InvestingStrategy is = new InvestingStrategy();
		is.start();
	}

	
}
