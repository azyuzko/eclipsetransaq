package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import ru.eclipsetrader.transaq.core.util.Utils;

@Embeddable
public class TakeProfit {
	@Column(name="tp_activationprice")
	Double activationprice;
	@Column(name="tp_guardtime")
	int guardtime;
	@Column(name="tp_brokerref")
	String brokerref;
	@Column(name="tp_quantity")
	String quantity; // :integer или :double (в случае %)
	Double extremum;
	@Column(name="tp_level")
	Double level;
	String correction;
	Double guardspread;
	
	@Override
	public String toString() {
		return Utils.toString(this);
	}

	public Double getActivationprice() {
		return activationprice;
	}

	public void setActivationprice(Double activationprice) {
		this.activationprice = activationprice;
	}

	public int getGuardtime() {
		return guardtime;
	}

	public void setGuardtime(int guardtime) {
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
