package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Signal {

	TQSymbol symbol;
	Date date;
	double price;
	BuySell buySell;
	String log;
	int quantity;
	
	public Signal(TQSymbol symbol, Date date,BuySell buySell, double price) {
		this.symbol = symbol;
		this.date = date;
		this.price = price;
		this.buySell = buySell;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public BuySell getBuySell() {
		return buySell;
	}

	public void setBuySell(BuySell buySell) {
		this.buySell = buySell;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public String toString() {
		return Utils.formatDate(getDate()) + "  " + getBuySell() + " price = " + getPrice() + " quantity = " + getQuantity();
	}
}
