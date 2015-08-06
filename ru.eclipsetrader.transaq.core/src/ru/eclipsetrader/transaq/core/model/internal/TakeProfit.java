package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

public class TakeProfit {
	Double activationprice;
	Date guardtime;
	String brokerref;
	String quantity; // :integer или :double (в случае %)
	Double extremum;
	Double level;
	String correction;
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

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
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

	public String getCorrection() {
		return correction;
	}

	public void setCorrection(String correction) {
		this.correction = correction;
	}

	public Double getGuardspread() {
		return guardspread;
	}

	public void setGuardspread(Double guardspread) {
		this.guardspread = guardspread;
	}

}
