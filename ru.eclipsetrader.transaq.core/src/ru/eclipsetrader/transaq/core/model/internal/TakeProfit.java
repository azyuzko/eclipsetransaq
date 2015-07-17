package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

public class TakeProfit {
	Double activationprice;
	Date guardtime;
	String brokerref;
	Double quantity; // :integer или :double (в случае %)
	Double extremum;
	Double level;
	Double correction;
	Double guardspread;

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

	public Double getExtremum() {
		return extremum;
	}

	public void setExtremum(Double extremum) {
		this.extremum = extremum;
	}

	public Double getLevel() {
		return level;
	}

	public void setLevel(Double level) {
		this.level = level;
	}

	public Double getCorrection() {
		return correction;
	}

	public void setCorrection(Double correction) {
		this.correction = correction;
	}

	public Double getGuardspread() {
		return guardspread;
	}

	public void setGuardspread(Double guardspread) {
		this.guardspread = guardspread;
	}

}
