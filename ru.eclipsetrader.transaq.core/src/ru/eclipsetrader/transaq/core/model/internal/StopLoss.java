package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

public class StopLoss {
	String usecredit;
	Double activationprice;
	Date guardtime;
	String brokerref;
	Double quantity; // :integer или :double (в случае %)
	Double orderprice;

	public String getUsecredit() {
		return usecredit;
	}

	public void setUsecredit(String usecredit) {
		this.usecredit = usecredit;
	}

	public Double getActivationprice() {
		return activationprice;
	}

	public void setActivationprice(Double activationprice) {
		this.activationprice = activationprice;
	}

	public Date getGuardtime() {
		return guardtime;
	}

	public void setGuardtime(Date guardtime) {
		this.guardtime = guardtime;
	}

	public String getBrokerref() {
		return brokerref;
	}

	public void setBrokerref(String brokerref) {
		this.brokerref = brokerref;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getOrderprice() {
		return orderprice;
	}

	public void setOrderprice(Double orderprice) {
		this.orderprice = orderprice;
	}

}
