package ru.eclipsetrader.transaq.core.strategy;

import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.server.TransaqServer;


public class TQStrategyService {
	
	static TQStrategyService instance;
	public static TQStrategyService getInstance() {
		if (instance == null) {
			instance = new TQStrategyService();
		}
		return instance;
	}

	Map<String, IStrategy> strategies = new HashMap<String, IStrategy>();

	public void start() {
		
		if (TransaqServer.getInstance() == null || TransaqServer.getInstance().getStatus() != ConnectionStatus.CONNECTED) {
			throw new RuntimeException("Not connected to server, cannot start");
		}

	}

}
