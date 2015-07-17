package ru.eclipsetrader.transaq.core.event.osgi;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.event.Event;

import ru.eclipsetrader.transaq.core.model.internal.ServerStatus;

public class OSGIServerStatusEvent {
	
	public static final String ID = "ru/transaq/events/server/ConnectionStatus";
	public static final String SERVER_ID = "serverId";
	public static final String STATUS = "status";
	
	public static org.osgi.service.event.Event getEvent(String serverId, ServerStatus newStatus) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(SERVER_ID, serverId);
		result.put(STATUS, newStatus);
		return new Event(ID, result);
	}
	
}
