package ru.eclipsetrader.transaq.core.datastorage;

import java.util.ArrayList;
import java.util.List;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.interfaces.ITQServer;
import ru.eclipsetrader.transaq.core.model.internal.Server;
import ru.eclipsetrader.transaq.core.services.ITQServerService;

public class TQServerService implements ITQServerService {

	static TQServerService instance;
	public static TQServerService getInstance() {
		if (instance == null) {
			instance = new TQServerService();
		}
		return instance;
	}
	
	private TQServerService() {
		
	}

	@Override
	public List<ITQServer> getServers() {
		return new ArrayList<ITQServer>(DataManager.getList(Server.class));
	}

	@Override
	public ITQServer getServer(String serverId) {
		return DataManager.get(Server.class, serverId);
	}

}
