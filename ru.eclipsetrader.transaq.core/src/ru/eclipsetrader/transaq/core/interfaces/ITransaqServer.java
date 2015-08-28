package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

public interface ITransaqServer {
	String getId();
	ConnectionStatus getStatus();
	void disconnect();
	Date getServerTime();
}
