package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.interfaces.ITQKey;

@Entity
@Table(name="fortsmoney_position")
@EntityListeners(DefaultJPAListener.class)
@IdClass(FortsMoneyPositionId.class)
public class FortsMoneyPosition extends ServerObject implements ITQKey {

	@Id
	String client;
	@Id
	Integer market;
	String shortname;
	@Column(name="CURR")
	double current;
	double blocked;
	double free;
	double varmargin;
	
	@Override
	public String getKey() {
		return String.valueOf(market);
	}

	
	public FortsMoneyPosition() {
		this(null);
	}

	public FortsMoneyPosition(String serverId) {
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

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public Integer getMarket() {
		return market;
	}

	public void setMarket(Integer market) {
		this.market = market;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	
}
