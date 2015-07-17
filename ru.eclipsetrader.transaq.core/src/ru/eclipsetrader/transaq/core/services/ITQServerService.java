package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.interfaces.ITQServer;

public interface ITQServerService {

	List<ITQServer> getServers();
	
	ITQServer getServer(String serverId);
	
}
