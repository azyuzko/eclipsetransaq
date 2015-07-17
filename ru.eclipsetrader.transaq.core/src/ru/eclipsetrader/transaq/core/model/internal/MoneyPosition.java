package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.interfaces.ITQKey;

@Entity
@Table(name="positions")
@EntityListeners(DefaultJPAListener.class)
public class MoneyPosition extends ServerObject implements ITQKey{
	
	@Id
	String client;
	@Id
	Integer market;
	@Id
	String register;
	@Id
	String asset;
	String shortname;
	
	double saldoin;
	double bought;
	double sold;
	double saldo;
	double ordbuy;
	double ordbuycond;
	double comission;
	
	@Override
	public String getKey() {
		return asset + Constants.SEPARATOR + register;
	}
	
	public MoneyPosition() {
		this(null);
	}
	
	public MoneyPosition(String serverId) {
		super(serverId);
	}

	public double getSaldoin() {
		return saldoin;
	}

	public void setSaldoin(double saldoin) {
		this.saldoin = saldoin;
	}

	public double getBought() {
		return bought;
	}

	public void setBought(double bought) {
		this.bought = bought;
	}

	public double getSold() {
		return sold;
	}

	public void setSold(double sold) {
		this.sold = sold;
	}

	public double getSaldo() {
		return saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	public double getOrdbuy() {
		return ordbuy;
	}

	public void setOrdbuy(double ordbuy) {
		this.ordbuy = ordbuy;
	}

	public double getOrdbuycond() {
		return ordbuycond;
	}

	public void setOrdbuycond(double ordbuycond) {
		this.ordbuycond = ordbuycond;
	}

	public double getComission() {
		return comission;
	}

	public void setComission(double comission) {
		this.comission = comission;
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

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	

}
