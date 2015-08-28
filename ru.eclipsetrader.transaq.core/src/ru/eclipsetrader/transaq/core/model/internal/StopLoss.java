package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import ru.eclipsetrader.transaq.core.util.Utils;

@Embeddable
public class StopLoss {
	String usecredit;
	@Column(name="sl_activationprice")
	Double activationprice;
	@Column(name="sl_guardtime")
	int guardtime;
	@Column(name="sl_brokerref")
	String brokerref;
	@Column(name="sl_quantity")
	String quantity; // :integer или :double (в случае %)
	Double orderprice;
	
	@Override
	public String toString() {
		return Utils.toString(this);
	}

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

	public Double getOrderprice() {
		return orderprice;
	}

	public void setOrderprice(Double orderprice) {
		this.orderprice = orderprice;
	}

}
