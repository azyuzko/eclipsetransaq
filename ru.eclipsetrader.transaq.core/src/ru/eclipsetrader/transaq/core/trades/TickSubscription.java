package ru.eclipsetrader.transaq.core.trades;

import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public class TickSubscription {

	TQSymbol symbol;
	String tradeno = "0";
	
	public TickSubscription(TQSymbol symbol, String tradeno) {
		this.symbol = symbol;
		this.tradeno = tradeno;
	}
	
	public TickSubscription(TQSymbol symbol) {
		this(symbol, "0");
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(TQSymbol symbol) {
		this.symbol = symbol;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TickSubscription) {
			TQSymbol ko1 = new TQSymbol(((TickSubscription)obj).symbol);
			TQSymbol ko2 = new TQSymbol(this.symbol);
			return ko1.equals(ko2);
		}
		return super.equals(obj);
	}
	
}
