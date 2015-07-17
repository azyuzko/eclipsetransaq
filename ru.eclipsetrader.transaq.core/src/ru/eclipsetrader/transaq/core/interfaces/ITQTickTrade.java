package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Date;

public interface ITQTickTrade {

	Date getTime();
	double getPrice();
	int getQuantity();
	
}
