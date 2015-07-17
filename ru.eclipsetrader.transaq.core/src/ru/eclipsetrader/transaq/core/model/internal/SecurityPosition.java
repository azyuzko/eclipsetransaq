package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.interfaces.ITQKey;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

@Entity
@Table(name="positions")
@EntityListeners(DefaultJPAListener.class)
public class SecurityPosition extends ServerObject implements ITQKey{
	
	Integer secid;
	@Id
	Integer market;
	@Id
	String seccode;
	String register;
	String client;
	String shortname;
	
	int saldoin;
	int saldomin;
	int bought;
	int sold;
	int saldo;
	int ordbuy;
	int ordsell;
	
	@Override
	public String getKey() {
		return String.valueOf(market) + TQSymbol.DELIMITER_MARKET + seccode;
	}
	
	public SecurityPosition() {
		this(null);
	}
	
	public SecurityPosition(String serverId) {
		super(serverId);
	}
	
	public int getSaldoin() {
		return saldoin;
	}
	public void setSaldoin(int saldoin) {
		this.saldoin = saldoin;
	}
	public int getSaldomin() {
		return saldomin;
	}
	public void setSaldomin(int saldomin) {
		this.saldomin = saldomin;
	}
	public int getBought() {
		return bought;
	}
	public void setBought(int bought) {
		this.bought = bought;
	}
	public int getSold() {
		return sold;
	}
	public void setSold(int sold) {
		this.sold = sold;
	}
	public int getSaldo() {
		return saldo;
	}
	public void setSaldo(int saldo) {
		this.saldo = saldo;
	}
	public int getOrdbuy() {
		return ordbuy;
	}
	public void setOrdbuy(int ordbuy) {
		this.ordbuy = ordbuy;
	}
	public int getOrdsell() {
		return ordsell;
	}
	public void setOrdsell(int ordsell) {
		this.ordsell = ordsell;
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

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	
}
