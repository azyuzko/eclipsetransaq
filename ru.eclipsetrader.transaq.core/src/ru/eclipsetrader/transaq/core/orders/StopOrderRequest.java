package ru.eclipsetrader.transaq.core.orders;

import java.util.Date;

import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

public class StopOrderRequest {
	
	TQSymbol symbol;
	String client;
	BuySell buySell;
	String linkedorderno;
	Date validfor;
	Date expdate;
	
	boolean hasStopLoss = false;
	Double  sl_activationprice;
	Integer	sl_guardtime;
	Double  sl_orderprice;
	Boolean sl_bymarket;
	Boolean sl_usecredit;
	String  sl_quantity;
	String  sl_brokerref;
	
	boolean hasTakeProfit;
	Double  tp_activationprice;
	Integer	tp_guardtime;
	Double  tp_orderprice;
	Boolean tp_bymarket;
	String  tp_quantity;
	boolean tp_usecredit;
	String  tp_brokerref;
	String  tp_correction = "0.0%";
	String  tp_spread;
	
	public StopOrderRequest(TQSymbol symbol) {
		this.symbol = symbol;
	}
	
	public String createCommand() {
		String command = "<command id=\"newstoporder\"> "
				+ "<security>"
				+ "<board>" + symbol.getBoard() + "</board>"
				+ "<seccode>" + symbol.getSeccode() + "</seccode>"
				+ "</security>"
				+ "<client>" + TQClientService.getInstance().getSecurityClientId(symbol) + "</client>"
				+ "<buysell>" + buySell + "</buysell>"
				+ ( linkedorderno != null ? "<linkedorderno>" + linkedorderno + "</linkedorderno>" : "")
				+ ( validfor != null ? "<validfor>" + Utils.formatDate(validfor)+"</validfor>" : "")
				+ "<expdate>дата экспирации (только для ФОРТС)</expdate>(не обязательно)"
				+ ( hasStopLoss ? "<stoploss>"
				+ "<activationprice>" + sl_activationprice + "</activationprice>"
				+ "<orderprice>" + sl_orderprice +"</orderprice>"
				+ (sl_bymarket ? "<bymarket/>" : "")
				+ "<quantity>"+ sl_quantity +"</quantity>"
				+ (sl_usecredit ? "<usecredit/>" : "")
				+ (sl_guardtime != null ? "<guardtime>" + Utils.formatGuardTime(sl_guardtime) +"</guardtime>" : "")
				+ (sl_brokerref != null ? "<brokerref>" + sl_brokerref +"</brokerref>" : "")
				+ "</stoploss>" : "")
				
				+ ( hasTakeProfit ? "<takeprofit>"
				+ "<activationprice>" + tp_activationprice + "</activationprice>"
				+ "<quantity>"+ tp_quantity +"</quantity>"
				+ (tp_usecredit ? "<usecredit/>" : "") 
				+ (tp_guardtime != null ? "<guardtime>" + Utils.formatGuardTime(tp_guardtime) +"</guardtime>" : "")
				+ (tp_brokerref != null ? "<brokerref>" + tp_brokerref +"</brokerref>" : "")
				+ "<correction>" + tp_correction + "</correction>"
				+ "<spread>" + tp_spread + "</spread>"
				+ (tp_bymarket ? "<bymarket/>" : "")
				+ "</takeprofit>" : "")
				
				+ "</command>";
		return command;
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(TQSymbol symbol) {
		this.symbol = symbol;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public BuySell getBuySell() {
		return buySell;
	}

	public void setBuySell(BuySell buySell) {
		this.buySell = buySell;
	}

	public String getLinkedorderno() {
		return linkedorderno;
	}

	public void setLinkedorderno(String linkedorderno) {
		this.linkedorderno = linkedorderno;
	}

	public Date getValidfor() {
		return validfor;
	}

	public void setValidfor(Date validfor) {
		this.validfor = validfor;
	}

	public Date getExpdate() {
		return expdate;
	}

	public void setExpdate(Date expdate) {
		this.expdate = expdate;
	}

	public boolean isHasStopLoss() {
		return hasStopLoss;
	}

	public void setHasStopLoss(boolean hasStopLoss) {
		this.hasStopLoss = hasStopLoss;
	}

	public Double getSl_activationprice() {
		return sl_activationprice;
	}

	public void setSl_activationprice(Double sl_activationprice) {
		this.sl_activationprice = sl_activationprice;
	}

	public Integer getSl_guardtime() {
		return sl_guardtime;
	}

	public void setSl_guardtime(Integer sl_guardtime) {
		this.sl_guardtime = sl_guardtime;
	}

	public Double getSl_orderprice() {
		return sl_orderprice;
	}

	public void setSl_orderprice(Double sl_orderprice) {
		this.sl_orderprice = sl_orderprice;
	}

	public Boolean getSl_bymarket() {
		return sl_bymarket;
	}

	public void setSl_bymarket(Boolean sl_bymarket) {
		this.sl_bymarket = sl_bymarket;
	}

	public Boolean getSl_usecredit() {
		return sl_usecredit;
	}

	public void setSl_usecredit(Boolean sl_usecredit) {
		this.sl_usecredit = sl_usecredit;
	}

	public String getSl_quantity() {
		return sl_quantity;
	}

	public void setSl_quantity(String sl_quantity) {
		this.sl_quantity = sl_quantity;
	}

	public String getSl_brokerref() {
		return sl_brokerref;
	}

	public void setSl_brokerref(String sl_brokerref) {
		this.sl_brokerref = sl_brokerref;
	}

	public boolean isHasTakeProfit() {
		return hasTakeProfit;
	}

	public void setHasTakeProfit(boolean hasTakeProfit) {
		this.hasTakeProfit = hasTakeProfit;
	}

	public Double getTp_activationprice() {
		return tp_activationprice;
	}

	public void setTp_activationprice(Double tp_activationprice) {
		this.tp_activationprice = tp_activationprice;
	}

	public Integer getTp_guardtime() {
		return tp_guardtime;
	}

	public void setTp_guardtime(Integer tp_guardtime) {
		this.tp_guardtime = tp_guardtime;
	}

	public Double getTp_orderprice() {
		return tp_orderprice;
	}

	public void setTp_orderprice(Double tp_orderprice) {
		this.tp_orderprice = tp_orderprice;
	}

	public Boolean getTp_bymarket() {
		return tp_bymarket;
	}

	public void setTp_bymarket(Boolean tp_bymarket) {
		this.tp_bymarket = tp_bymarket;
	}

	public String getTp_quantity() {
		return tp_quantity;
	}

	public void setTp_quantity(String tp_quantity) {
		this.tp_quantity = tp_quantity;
	}

	public boolean isTp_usecredit() {
		return tp_usecredit;
	}

	public void setTp_usecredit(boolean tp_usecredit) {
		this.tp_usecredit = tp_usecredit;
	}

	public String getTp_brokerref() {
		return tp_brokerref;
	}

	public void setTp_brokerref(String tp_brokerref) {
		this.tp_brokerref = tp_brokerref;
	}

	public String getTp_correction() {
		return tp_correction;
	}

	public void setTp_correction(String tp_correction) {
		this.tp_correction = tp_correction;
	}

	public String getTp_spread() {
		return tp_spread;
	}

	public void setTp_spread(String tp_spread) {
		this.tp_spread = tp_spread;
	}
	
	
}
