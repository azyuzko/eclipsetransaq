package ru.eclipsetrader.transaq.core.server;

import java.util.List;
import java.util.function.Consumer;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.exception.ConnectionException;
import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.services.ITransaqServerManager;

public class TransaqServerManager implements ITransaqServerManager {

	static TransaqServerManager instance;
	public static ITransaqServerManager getInstance() {
		if (instance == null) {
			instance = new TransaqServerManager();
		}
		return instance;
	}
	
	private TransaqServerManager(){
		
	}

	
	@Override
	public ITransaqServer getActiveTransaqServer() {
		return TransaqServer.getInstance();
	}


	@Override
	public ConnectionStatus getStatus(String serverId) {
		return ConnectionStatus.DISCONNECTED;
	}

	@Override
	public List<String> getServers() {
		@SuppressWarnings("unchecked")
		List<String> result = DataManager.executeQuery("select s.id from Server s");
		return result;
	}

	@Override
	public void reconnect() {
		if (TransaqServer.getInstance() == null) {
			throw new RuntimeException("Cannot reconnect empty server!");
		}
		TransaqServer.getInstance().reconnect();
	}

	@Override
	public ITransaqServer connect(String serverId) {
		return connect(serverId, (c) -> {});
	}

	@Override
	public void disconnect() {
		if (TransaqServer.getInstance() == null) {
			throw new RuntimeException("Not connected to server!");
		}
		TransaqServer.getInstance().disconnect();
		TransaqServer.setTransaqServer(null);
	}

	@Override
	public ITransaqServer connect(String serverId,
			Consumer<TransaqServer> callback) {
		if (TransaqServer.getInstance() != null) {
			throw new RuntimeException("Already connected to server!");
		}
		
		TransaqServer instance = new TransaqServer(serverId);
		
		try {
			instance.connect(serverId, callback);
			TransaqServer.setTransaqServer(instance);
			return instance;
		} catch (ConnectionException e) {
			throw new RuntimeException(e.getMessage());
		}
	}


}
