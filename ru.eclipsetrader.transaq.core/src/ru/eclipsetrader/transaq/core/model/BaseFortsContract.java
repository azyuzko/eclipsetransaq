package ru.eclipsetrader.transaq.core.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ru.eclipsetrader.transaq.core.model.internal.ServerObject;

@MappedSuperclass
public class BaseFortsContract extends ServerObject implements Cloneable {


	int startnet;
	int openbuys; // ордера на покупку
	int opensells;// ордера на продажу

	int todaybuy; // всего куплено
	int todaysell;// всего продано
	double optmargin;
	double varmargin; // вариационная маржа
	
	public double getGO() {
		return Math.max( Math.abs((getTotalnet() + (openbuys - opensells)) * price), Math.abs(getTotalnet() * price));
	}

	@Override
	public String toString() {
		return " startnet: " + startnet + " openbuys:" + openbuys + " opensells:"+ opensells + " totalnet:" +getTotalnet()+
			" todaybuy:" + todaybuy + " todaysell:" + todaysell + " GO:" + getGO() + " varmargin:" + varmargin;
	}
	
	/**
	 * Взаимозачет открытых заявок на покупку/продажу
	 */
	public int getOpennet() {
		return openbuys - opensells;
	}
	
	/**
	 * Считаем изменение ГО при покупке quantity контрактов
	 * @param quantity
	 * @return
	 */
	public double calcBuy(int quantity) {
		double diffGO = 0.0; // возвращаемый результат
		double oldGo = getGO();
		int oldOpenbuys = openbuys; // сохраним старое значение кол-ва покупок
		try {
			openbuys = openbuys + quantity; // откроем позиции на покупку
			if (opensells > openbuys) {
				// do nothing ГО не изменится
			} else {
				double newGo = getGO();
				diffGO = newGo - oldGo;
			}
		} finally {
			openbuys = oldOpenbuys; // восстановим старое значение
		}
		return diffGO;
	}
	
	/**
	 * Считаем изменение ГО при продаже quantity контрактов
	 * @param quantity
	 * @return
	 */
	public double calcSell(int quantity) {
		double diffGO = 0.0; // возвращаемый результат
		double oldGo = getGO();
		int oldOpensells = opensells; // сохраним старое значение кол-ва покупок
		try {
			opensells = opensells + quantity; // откроем позиции на покупку
			if (openbuys > opensells) {
				// do nothing ГО не изменится
			} else {
				double newGo = getGO();
				diffGO = newGo - oldGo;
			}
		} finally {
			opensells = oldOpensells; // восстановим старое значение
		}
		return diffGO;
	}
	
	@Transient
	double price;

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public BaseFortsContract() {
		this(null);
	}

	public BaseFortsContract(String serverId) {
		super(serverId);
	}
	

	public int getStartnet() {
		return startnet;
	}

	public void setStartnet(int startnet) {
		this.startnet = startnet;
	}

	public int getOpenbuys() {
		return openbuys;
	}

	public void setOpenbuys(int openbuys) {
		this.openbuys = openbuys;
	}

	public int getOpensells() {
		return opensells;
	}

	public void setOpensells(int opensells) {
		this.opensells = opensells;
	}

	public int getTotalnet() {
		return todaybuy - todaysell;
	}

	public int getTodaybuy() {
		return todaybuy;
	}

	public void setTodaybuy(int todaybuy) {
		this.todaybuy = todaybuy;
	}

	public int getTodaysell() {
		return todaysell;
	}

	public void setTodaysell(int todaysell) {
		this.todaysell = todaysell;
	}

	public double getOptmargin() {
		return optmargin;
	}

	public void setOptmargin(double optmargin) {
		this.optmargin = optmargin;
	}

	public double getVarmargin() {
		return varmargin;
	}

	public void setVarmargin(double varmargin) {
		this.varmargin = varmargin;
	}
	
	@Override
	public BaseFortsContract clone() {
		BaseFortsContract newObject = new BaseFortsContract();
		newObject.setOpenbuys(openbuys);
		newObject.setOpensells(opensells);
		newObject.setOptmargin(optmargin);
		newObject.setPrice(price);
		newObject.setServer(getServer());
		newObject.setStartnet(startnet);
		newObject.setTodaybuy(todaybuy);
		newObject.setTodaysell(todaysell);
		newObject.setVarmargin(varmargin);
		return newObject;
	}

	public static void main(String[] args) {
		BaseFortsContract contract = new BaseFortsContract();
		contract.setPrice(50.0);
		contract.setOpenbuys(3);
		contract.setOpensells(0);
		contract.setTodaybuy(5);
		contract.setTodaysell(6);
		System.out.println(contract);
	}
}
