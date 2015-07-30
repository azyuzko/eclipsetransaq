package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IAccount {

	/**
	 * Buy by markey
	 * @param symbol
	 * @param quantity
	 */
	void buy(TQSymbol symbol, int quantity);
	
	/**
	 * Sell by market
	 * @param symbol
	 * @param quantity
	 */
	void sell(TQSymbol symbol, int quantity);
	
	/**
	 * �������� � �������
	 * @param symbol
	 * @return
	 */
	int availableToBuy(TQSymbol symbol);
	
	/**
	 * �������� � �������
	 * @param symbol
	 * @return
	 */
	int availableToSell(TQSymbol symbol);
	
	void cancelOpenOrders(TQSymbol symbol);
	void closePosition(TQSymbol symbol);
	
}
