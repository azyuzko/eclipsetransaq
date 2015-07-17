package ru.eclipsetrader.transaq.core.model.internal;


public class Pit extends BaseObject {

	String secCode;

	String board;
	Integer market;
	int decimals;
	double minStep;
	int lotSize;
	double point_cost;

	public String getKey() {
		return board+"_"+secCode;
	}
	
	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public Integer getMarket() {
		return market;
	}

	public void setMarket(Integer market) {
		this.market = market;
	}

	public int getDecimals() {
		return decimals;
	}

	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}

	public double getMinStep() {
		return minStep;
	}

	public void setMinStep(double minStep) {
		this.minStep = minStep;
	}

	public int getLotSize() {
		return lotSize;
	}

	public void setLotSize(int lotSize) {
		this.lotSize = lotSize;
	}

	public double getPoint_cost() {
		return point_cost;
	}

	public void setPoint_cost(double point_cost) {
		this.point_cost = point_cost;
	}

}
