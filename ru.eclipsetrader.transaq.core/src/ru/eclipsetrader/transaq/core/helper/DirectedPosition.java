package ru.eclipsetrader.transaq.core.helper;

import ru.eclipsetrader.transaq.core.model.BuySell;

public class DirectedPosition {

	BuySell buySell;
	double open;
	double close;
	double stopLoss;
	double takeProfit;
	
	public DirectedPosition invert() {
		DirectedPosition d = new DirectedPosition();
		d.buySell = buySell.reverted();
		double temp = close;
		d.close = open;
		d.open = temp;
		temp = stopLoss;
		d.stopLoss = takeProfit;
		d.takeProfit = temp;
		return d;
	}
	
}
