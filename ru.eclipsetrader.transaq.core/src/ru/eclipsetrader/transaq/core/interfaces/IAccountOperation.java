package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.BuySell;

public interface IAccountOperation {

	Date getTime();
	BuySell getBuySell();
	
}
