package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

public interface ITransaqServerManager {
	
	List<String> getServers();

	ITransaqServer getActiveTransaqServer();

	ConnectionStatus getStatus(String serverId);
	
	ITransaqServer connect(String serverId);
	
	void disconnect();
	
	void reconnect();
}
