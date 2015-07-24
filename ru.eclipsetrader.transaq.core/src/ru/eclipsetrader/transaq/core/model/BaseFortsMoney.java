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
