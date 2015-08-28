package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.model.BaseFortsContract;

@Entity
@Table(name="forts_position")
@IdClass(FortsPositionId.class)
public class FortsPosition extends BaseFortsContract {

	@Id
	String seccode;
	@Id
	Integer market;
	@Id
	String client;
	
	int totalnet; // взаимозачет
	int expirationpos;
	Double usedsellspotlimit;
	Double sellspotlimit;
	Double netto;
	Double kgo;
	
	Integer secid;
	
	public FortsPosition() {
		this(null);
	}

	public FortsPosition(String serverId) {
		super(serverId);
	}
	
	public int getTotalnet() {
		return totalnet;
	}

	public void setTotalnet(int totalnet) {
		this.totalnet = totalnet;
	}

	public int getExpirationpos() {
		return expirationpos;
	}

	public void setExpirationpos(int expirationpos) {
		this.expirationpos = expirationpos;
	}

	public Double getUsedsellspotlimit() {
		return usedsellspotlimit;
	}

	public void setUsedsellspotlimit(Double usedsellspotlimit) {
		this.usedsellspotlimit = usedsellspotlimit;
	}

	public Double getSellspotlimit() {
		return sellspotlimit;
	}

	public void setSellspotlimit(Double sellspotlimit) {
		this.sellspotlimit = sellspotlimit;
	}

	public Double getNetto() {
		return netto;
	}

	public void setNetto(Double netto) {
		this.netto = netto;
	}

	public Double getKgo() {
		return kgo;
	}

	public void setKgo(Double kgo) {
		this.kgo = kgo;
	}

	public Integer getMarket() {
		return market;
	}

	public void setMarket(Integer market) {
		this.market = market;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}


	public String getSeccode() {
		return seccode;
	}


	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	
}
