package ru.eclipsetrader.transaq.core.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ru.eclipsetrader.transaq.core.model.internal.ServerObject;

@MappedSuperclass
public class BaseFortsContract extends ServerObject {


	int startnet;
	int openbuys; // ордера на покупку
	int opensells;// ордера на продажу
	int totalnet; // взаимозачет
	int todaybuy; // всего куплено
	int todaysell;// всего продано
	double optmargin;
	double varmargin; // вариационная маржа

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
		return totalnet;
	}

	public void setTotalnet(int totalnet) {
		this.totalnet = totalnet;
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

}
