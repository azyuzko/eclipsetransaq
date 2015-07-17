package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

public interface ITransaqServer {
	String getId();
	String getSessionId();
	ConnectionStatus getStatus();
	void disconnect();
}
