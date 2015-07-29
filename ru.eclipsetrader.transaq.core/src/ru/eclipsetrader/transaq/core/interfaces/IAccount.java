package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IAccount {

	void buy(TQSymbol symbol, int quantity);
	void sell(TQSymbol symbol, int quantity);
	
	void cancelOpenOrders(TQSymbol symbol);
	void closePosition(TQSymbol symbol);
	
}
