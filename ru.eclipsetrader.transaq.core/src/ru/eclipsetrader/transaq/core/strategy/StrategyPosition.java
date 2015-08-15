package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

public class StrategyPosition {

	TQSymbol symbol;
	Date openDate;
	Date closeDate;

	double openCost;
	double closeCost;
	int quantity;

	BuySell buySell;

	String openLog;
	String closeLog;

	public StrategyPosition(TQSymbol symbol, BuySell buySell) {
		this.symbol = symbol;
		this.buySell = buySell;
	}

	public double getPlanProfit(double price) {
		switch (buySell) {
		case B: return price*quantity - openCost;	
		case S: return openCost - price*quantity;
		}
		return 0;
	}
	
	public double getProfit() {
		if (closeDate != null) {
			switch (buySell) {
			case B: return closeCost - openCost;	
			case S: return openCost - closeCost;
			}
		}
		return 0;
	}

	public BuySell getBuySell() {
		return buySell;
	}

	public Date getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Date openDate) {
		this.openDate = openDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public double getOpenCost() {
		return openCost;
	}

	public void setOpenCost(double openCost) {
		this.openCost = openCost;
	}

	public double getCloseCost() {
		return closeCost;
	}

	public void setCloseCost(double closeCost) {
		this.closeCost = closeCost;
	}

	public String getOpenLog() {
		return openLog;
	}

	public void setOpenLog(String openLog) {
		this.openLog = openLog;
	}

	public String getCloseLog() {
		return closeLog;
	}

	public void setCloseLog(String closeLog) {
		this.closeLog = closeLog;
	}

	@Override
	public String toString() {
		return buySell + ":  opened " + Utils.formatTime(openDate) + " = " + openCost + ", closed " + Utils.formatTime(closeDate) + " = " + closeCost + ", quantity = " + getQuantity()
				 +  (closeDate != null ? " profit = " + getProfit() : "" );
	}
}
