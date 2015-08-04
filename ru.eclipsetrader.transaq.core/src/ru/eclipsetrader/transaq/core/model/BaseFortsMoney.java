package ru.eclipsetrader.transaq.core.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import ru.eclipsetrader.transaq.core.model.internal.ServerObject;

@MappedSuperclass
public class BaseFortsMoney extends ServerObject {

	@Column(name="CURR")
	double current;
	double blocked;
	double free;
	double varmargin;
	
	@Override
	public String toString() {
		return "Current = " + getCurrent() + " Blocked = " + getBlocked() + " Free = " + getFree() + " Varmargin = " + getVarmargin();
	}
	
	/**
	 * ������������� ������ ��������� �������
	 * @param oldGO ������ ������ ��
	 * @param newGO ����� ������ ��
	 * @return ������� ����� ������ � �����
	 */
	public double recalcGO(double oldGO, double newGO) {
		double diff = oldGO - newGO;
		free += diff;
		blocked -= diff;
		return diff;
	}
	
	/**
	 * ������������� ������ ��������� �������
	 * @param diff ������� ����� ������ ��������� �� � �����
	 */
	public void recalcGO(double diff) {
		free += diff;
		blocked -= diff;
	}
	
	public BaseFortsMoney() {
		this(null);
	}
	
	public BaseFortsMoney(String serverId) {
		super(serverId);
	}


	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
	}

	public double getBlocked() {
		return blocked;
	}

	public void setBlocked(double blocked) {
		this.blocked = blocked;
	}

	public double getFree() {
		return free;
	}

	public void setFree(double free) {
		this.free = free;
	}

	public double getVarmargin() {
		return varmargin;
	}

	public void setVarmargin(double varmargin) {
		this.varmargin = varmargin;
	}

}
