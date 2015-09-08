package ru.eclipsetrader.transaq.core.services;

import java.util.List;
import java.util.function.Consumer;

import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.server.TransaqServer;

public interface ITransaqServerManager {
	
	List<String> getServers();

	ITransaqServer getActiveTransaqServer();

	ConnectionStatus getStatus(String serverId);
	
	ITransaqServer connect(String serverId);
	ITransaqServer connect(String serverId, Consumer<TransaqServer> callback);
	
	void disconnect();
	
	void reconnect();
}
