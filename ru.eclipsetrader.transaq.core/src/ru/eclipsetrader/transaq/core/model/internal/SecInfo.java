package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

public class SecInfo extends BaseObject {

	String sec_id;
	String secname;
	String seccode;
	Integer market;
	String pname;
	Date mat_date;
	Double clearing_price;
	Double minprice;
	Double maxprice;
	Double buy_deposit;
	Double sell_deposit;
	Double bgo_c;
	Double bgo_nc;
	Double accruedint;
	Double coupon_value;
	Date coupon_date;
	Date coupon_period;
	Double facevalue;
	String put_call;
	String opt_type;
	int lot_volume;
	
	@Override
	public String getKey() {
		return seccode;
	}	

}
