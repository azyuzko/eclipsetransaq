package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

public interface ITransaqServer {
	String getId();
	ConnectionStatus getStatus();
	void disconnect();
}
