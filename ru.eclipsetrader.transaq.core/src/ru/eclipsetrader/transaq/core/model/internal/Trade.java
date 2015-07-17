package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;

@Table(name="trades")
@Entity(name="Trade")
@EntityListeners(DefaultJPAListener.class)
public class Trade extends Tick  {

	String orderno;
	String client;
	String brokerref;
	Double value;
	Double comission;
	Double yield;
	Double accruedint;
	String tradetype;
	String settlecode;
	double currentpos;
	
	int items;

	public int getItems() {
		return items;
	}

	public void setItems(int items) {
		this.items = items;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getBrokerref() {
		return brokerref;
	}

	public void setBrokerref(String brokerref) {
		this.brokerref = brokerref;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getComission() {
		return comission;
	}

	public void setComission(Double comission) {
		this.comission = comission;
	}

	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
	}

	public Double getAccruedint() {
		return accruedint;
	}

	public void setAccruedint(Double accruedint) {
		this.accruedint = accruedint;
	}

	public String getTradetype() {
		return tradetype;
	}

	public void setTradetype(String tradetype) {
		this.tradetype = tradetype;
	}

	public String getSettlecode() {
		return settlecode;
	}

	public void setSettlecode(String settlecode) {
		this.settlecode = settlecode;
	}

	public double getCurrentpos() {
		return currentpos;
	}

	public void setCurrentpos(double currentpos) {
		this.currentpos = currentpos;
	}

}
