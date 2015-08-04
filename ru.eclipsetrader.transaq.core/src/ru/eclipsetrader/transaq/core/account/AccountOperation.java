package ru.eclipsetrader.transaq.core.account;

import java.util.Date;

import ru.eclipsetrader.transaq.core.interfaces.IAccountOperation;
import ru.eclipsetrader.transaq.core.model.BuySell;

public class AccountOperation extends QuantityCost implements IAccountOperation {
	
	BuySell buySell;
	Date time;
	
	public AccountOperation(BuySell buySell, Date time, int quantity, double cost) {
		super(quantity, cost);
		this.buySell = buySell;
		this.time = time;
	}

	@Override
	public Date getTime() {
		return time;
	}

	@Override
	public BuySell getBuySell() {
		return buySell;
	}

}
