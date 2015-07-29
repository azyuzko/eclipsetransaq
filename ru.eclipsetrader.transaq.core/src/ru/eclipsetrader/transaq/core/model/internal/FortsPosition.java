package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.interfaces.ITQKey;
import ru.eclipsetrader.transaq.core.model.BaseFortsContract;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

@Entity
@Table(name="forts_position")
@EntityListeners(DefaultJPAListener.class)
@IdClass(FortsPositionId.class)
public class FortsPosition extends BaseFortsContract implements ITQKey {

	Integer secid;
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
	
	@Override
	public String getKey() {
		return String.valueOf(market) + TQSymbol.DELIMITER_MARKET + seccode;
	}
	
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

	public Integer getSecid() {
		return secid;
	}

	public void setSecid(Integer secid) {
		this.secid = secid;
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
