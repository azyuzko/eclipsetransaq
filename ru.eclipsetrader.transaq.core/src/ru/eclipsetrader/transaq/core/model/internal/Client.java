package ru.eclipsetrader.transaq.core.model.internal;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import ru.eclipsetrader.transaq.core.model.ClientType;
import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
public class Client extends ServerObject {

	public Client(){
		super(null);
	}
	
	public Client(String serverId) {
		super(serverId);
	}

	@Id
	String id;
	boolean remove;
	String currency;
	@Enumerated(EnumType.STRING)
	ClientType type;

	double ml_intraday;
	double ml_overnight;

	double ml_restrict;
	double ml_call;
	double ml_close;
	
	@OneToMany
	@JoinColumn(name = "client")
	@MapKey(name = "tradeno")
	Map<String, Trade> trades = new HashMap<String, Trade>();

	

	public String getKey() {
		return id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public ClientType getType() {
		return type;
	}

	public void setType(ClientType type) {
		this.type = type;
	}

	public double getMl_intraday() {
		return ml_intraday;
	}

	public void setMl_intraday(double ml_intraday) {
		this.ml_intraday = ml_intraday;
	}

	public double getMl_overnight() {
		return ml_overnight;
	}

	public void setMl_overnight(double ml_overnight) {
		this.ml_overnight = ml_overnight;
	}

	public double getMl_restrict() {
		return ml_restrict;
	}

	public void setMl_restrict(double ml_restrict) {
		this.ml_restrict = ml_restrict;
	}

	public double getMl_call() {
		return ml_call;
	}

	public void setMl_call(double ml_call) {
		this.ml_call = ml_call;
	}

	public double getMl_close() {
		return ml_close;
	}

	public void setMl_close(double ml_close) {
		this.ml_close = ml_close;
	}

	public Map<String, Trade> getTrades() {
		return trades;
	}
	
	public String toString() {
		return Utils.toString(this);	
	}

}
