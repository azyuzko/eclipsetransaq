package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;
import ru.eclipsetrader.transaq.core.model.BaseFortsMoney;

@Entity
@Table(name="fortsmoney_position")
public class FortsMoneyPosition extends BaseFortsMoney implements ITQKey {

	@Id
	String client;
	Integer market;
	String shortname;
	
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
