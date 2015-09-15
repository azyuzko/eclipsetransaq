package ru.eclipsetrader.transaq.core.quotes;

public class Spread {

	double buy;
	double sell;
	
	public Spread(double buy, double sell) {
		this.buy = buy;
		this.sell = sell;
	}
	
	@Override
	public String toString() {
		return "B: " + buy + "  S:" + sell;
	}
	
	public double getBuy() {
		return buy;
	}
	public void setBuy(double buy) {
		this.buy = buy;
	}
	public double getSell() {
		return sell;
	}
	public void setSell(double sell) {
		this.sell = sell;
	}
	
	
	
}
