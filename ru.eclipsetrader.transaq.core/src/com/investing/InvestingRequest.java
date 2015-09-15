package com.investing;

import java.util.Date;

import ru.eclipsetrader.transaq.core.quotes.Spread;
import ru.eclipsetrader.transaq.core.util.Utils;

public class InvestingRequest {

	Date date;
	double price;
	InvestingState signal;
	Date lastUpdate;
	
	@Override
	public String toString() {
		return Utils.formatDate(lastUpdate) + "  " + price + "  " + signal;
	}
}
