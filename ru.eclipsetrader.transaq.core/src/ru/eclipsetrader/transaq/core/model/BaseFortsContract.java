package ru.eclipsetrader.transaq.core.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ru.eclipsetrader.transaq.core.model.internal.ServerObject;

@MappedSuperclass
public class BaseFortsContract extends ServerObject implements Cloneable {


	int startnet;
	int openbuys; // ������ �� �������
	int opensells;// ������ �� �������

	int todaybuy; // ����� �������
	int todaysell;// ����� �������
	double optmargin;
	double varmargin; // ������������ �����
	
	public double getGO() {
		return Math.max( Math.abs((getTotalnet() + (openbuys - opensells)) * price), Math.abs(getTotalnet() * price));
	}

	@Override
	public String toString() {
		return " startnet: " + startnet + " openbuys:" + openbuys + " opensells:"+ opensells + " totalnet:" +getTotalnet()+
			" todaybuy:" + todaybuy + " todaysell:" + todaysell + " GO:" + getGO() + " varmargin:" + varmargin;
	}
	
	/**
	 * ����������� �������� ������ �� �������/�������
	 */
	public int getOpennet() {
		return openbuys - opensells;
	}
	
	/**
	 * ������� ��������� �� ��� ������� quantity ����������
	 * @param quantity
	 * @return
	 */
	public double calcBuy(int quantity) {
		double diffGO = 0.0; // ������������ ���������
		double oldGo = getGO();
		int oldOpenbuys = openbuys; // �������� ������ �������� ���-�� �������
		try {
			openbuys = openbuys + quantity; // ������� ������� �� �������
			if (opensells > openbuys) {
				// do nothing �� �� ���������
			} else {
				double newGo = getGO();
				diffGO = newGo - oldGo;
			}
		} finally {
			openbuys = oldOpenbuys; // ����������� ������ ��������
		}
		return diffGO;
	}
	
	/**
	 * ������� ��������� �� ��� ������� quantity ����������
	 * @param quantity
	 * @return
	 */
	public double calcSell(int quantity) {
		double diffGO = 0.0; // ������������ ���������
		double oldGo = getGO();
		int oldOpensells = opensells; // �������� ������ �������� ���-�� �������
		try {
			opensells = opensells + quantity; // ������� ������� �� �������
			if (openbuys > opensells) {
				// do nothing �� �� ���������
			} else {
				double newGo = getGO();
				diffGO = newGo - oldGo;
			}
		} finally {
			opensells = oldOpensells; // ����������� ������ ��������
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
