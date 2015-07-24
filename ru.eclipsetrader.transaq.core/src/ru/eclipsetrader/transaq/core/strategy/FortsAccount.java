package ru.eclipsetrader.transaq.core.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.PSource;

import ru.eclipsetrader.transaq.core.model.BaseFortsContract;
import ru.eclipsetrader.transaq.core.model.BaseFortsMoney;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Holder;

public class FortsAccount {

	private static final double COMISSION_RATE = 0.0001; // % от суммы сделки
	
	BaseFortsMoney money = new BaseFortsMoney();
	double comission = 0;
	
	Map<TQSymbol, BaseFortsContract> positions = new HashMap<>();
	
	public FortsAccount(double current) {
		money.setCurrent(current);
		money.setFree(current);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FORTS Money: Current = " + money.getCurrent() + " Blocked = " + money.getBlocked() + " Free = " + money.getFree() + " Varmargin = " + money.getVarmargin() + "\n");
		
		for (TQSymbol symbol : positions.keySet()) {
			BaseFortsContract contract = positions.get(symbol);
			sb.append(symbol + " startnet: " + contract.getStartnet() + " openbuys:" + contract.getOpenbuys() + " opensells:"+ contract.getOpensells() + " totalnet:" +contract.getTotalnet()+
					" todaybuy:" + contract.getTodaybuy() + " todaysell:" + contract.getTodaysell() + " optmargin:" + contract.getOptmargin() + " varmargin:" + contract.getVarmargin());
		}
		return sb.toString();
	}

	public double getFree() {
		return money.getFree();
	}
	
	public double getCurrent() {
		return money.getCurrent();
	}
	
	public double getBlocked() {
		return money.getBlocked();
	}
	
	public double getVarmargin() {
		return money.getVarmargin();
	}
	
	public boolean lockBuy(TQSymbol symbol, int quantity, double price) {
		BaseFortsContract position = positions.get(symbol);
		if (position == null) {
			position = new BaseFortsContract();
			position.setPrice(price);
		}
		// если открытых на продажу больше чем на покупку + quantity
		if (position.getOpensells() >= (position.getOpenbuys()+quantity) ) {
			// только увеличим число открытых
			position.setOpenbuys(position.getOpenbuys() + quantity);
			// сободные ср-ва не измен€тс€
			positions.put(symbol, position);
			return true;
		} 
		
		int nett = (position.getOpenbuys()+quantity) - position.getOpensells(); // возьмем разницу
		double amount = nett * position.getPrice();
		
		// если свободных средств не хватает - не работаем
		if (money.getFree() < amount) {
			return false;
			
		} else {
			// свободных хватает - работаем
			money.setFree(money.getFree() - amount);
			money.setBlocked(money.getBlocked() + amount);
			position.setOpenbuys(position.getOpenbuys() + quantity);
			positions.put(symbol, position);
			return true;
		}
	}
	
	public boolean lockSell(TQSymbol symbol, int quantity, double price) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null) {
			position = new BaseFortsContract();
			position.setPrice(price);
		}
		
		// если открытых на покупку больше чем на продажу + quantity
		if (position.getOpenbuys() >= (position.getOpensells()+quantity) ) {
			// только увеличим число открытых
			position.setOpensells(position.getOpensells() + quantity);
			// сободные ср-ва не измен€тс€
			positions.put(symbol, position);
			return true;
		} 
		
		int nett = (position.getOpensells()+quantity) - position.getOpenbuys(); // возьмем разницу
		double amount = nett * position.getPrice();
		// если свободных средств не хватает - не работаем
		if (money.getFree() < amount) {
			return false;
			
		} else {
			// свободных хватает - работаем
			money.setFree(money.getFree() - amount);
			money.setBlocked(money.getBlocked() + amount);
			position.setOpensells(position.getOpensells() + quantity);
			positions.put(symbol, position);
			return true;
		}
	}
	
	/**
	 * ѕокупка только из очереди на покупку!
	 * @param symbol
	 * @param quantity
	 * @return
	 */
	public void buy(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null || position.getOpenbuys() < quantity) {
			throw new RuntimeException("¬ очереди нет " + quantity + " контрактов на покупку " + symbol);
		}
		
		position.setOpenbuys(position.getOpenbuys() - quantity);
		position.setTodaybuy(position.getTodaybuy() + quantity);
		position.setTotalnet(Math.abs(position.getTodaybuy() - position.getTodaysell()));
	}

	
	public void sell(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null || position.getOpensells() < quantity) {
			throw new RuntimeException("¬ очереди нет " + quantity + " контрактов на продажу " + symbol);
		}
		
		position.setOpensells(position.getOpensells() - quantity);
		position.setTodaysell(position.getTodaysell() + quantity);
		position.setTotalnet(Math.abs(position.getTodaybuy() - position.getTodaysell()));
	}
	
	/**
	 * ѕересчитывает вариационную маржу
	 * @param symbol
	 * @param price
	 */
	public void recalc(TQSymbol symbol, double price) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			double diff = position.getPrice() - price;
			double varmargin = (position.getTotalnet() + Math.abs(position.getOpenbuys()-position.getOpensells())) * diff;
			position.setVarmargin(varmargin);
		}
	}
	
	public void close(TQSymbol symbol, double buyPrice, double sellPrice) {
		
	}
	
	public static void main(String[] args) {
		FortsAccount a = new FortsAccount(1000);
		
		TQSymbol s1 = new TQSymbol(BoardType.FUT, "SiU5");
		
		System.out.println(a);

		a.lockBuy(s1, 1, 50.0);
		a.lockSell(s1, 2, 60.0);
		a.buy(s1, 1);
		a.sell(s1, 1);
		
		System.out.println(a);

		a.recalc(s1, 51.0);

		System.out.println(a);

		
	}

}
