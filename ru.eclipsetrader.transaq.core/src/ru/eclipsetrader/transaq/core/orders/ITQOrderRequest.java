package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;

public interface ITQOrderRequest {

	public int getQuantity();	
	public ITQSecurity getSecurity();
	public double getPrice();
	public boolean isCancelled();
	public String getBrokerref();
	
}
