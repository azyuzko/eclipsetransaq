package ru.eclipsetrader.transaq.core.orders;

import java.util.Date;

import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.UnfilledType;
import ru.eclipsetrader.transaq.core.util.Utils;

public class OrderRequest {

	TQSymbol symbol;
	double price;
	int quantity;
	int hidden;
	BuySell buysell;
	boolean byMarket = false;
	String brokerref;
	private UnfilledType unfilled = UnfilledType.CancelBalance; // немедленно, или отменить ImmOrCancel не работает для FUT
	private boolean usecredit;
	private boolean nosplit;
	private Date expdate;
	
	private OrderRequest(TQSymbol symbol, BuySell bs, int quantity) {
		this.symbol = symbol;
		this.quantity = quantity;
		this.buysell = bs;
	}

	public static OrderRequest createRequest(TQSymbol symbol, BuySell bs, double price, int quantity) {
		OrderRequest orderRequest = new OrderRequest(symbol, bs, quantity);
		orderRequest.price = price;
		return orderRequest;
	}
	
	public static OrderRequest createByMarketRequest(TQSymbol symbol, BuySell bs, int quantity) {
		OrderRequest orderRequest = new OrderRequest(symbol, bs, quantity);
		orderRequest.byMarket = true;
		return orderRequest;
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(TQSymbol symbol) {
		this.symbol = symbol;
	}

	public int getHidden() {
		return hidden;
	}

	public void setHidden(int hidden) {
		this.hidden = hidden;
	}

	public double getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public String createNewOrderCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"neworder\">");
		sb.append("<security>");
		sb.append("<board>" + symbol.getBoard() + "</board>");
		sb.append("<seccode>" + symbol.getSeccode() + "</seccode>");
		sb.append("</security>");
		sb.append("<client>" + TQClientService.getInstance().getSecurityClientId(symbol) + "</client>");
		if (price != 0) {
			sb.append("<price>" + price + "</price>");
		}
		
		if (hidden != 0) {
			sb.append("<hidden>" + hidden + "</hidden>");
		}
		
		sb.append("<quantity>" + quantity + "</quantity>");
		sb.append("<buysell>" + buysell.name() + "</buysell>");
		
		if (byMarket) {
			sb.append("<bymarket/>");
		}
		
		if (brokerref != null) {
			sb.append("<brokerref>" + brokerref + "</brokerref>");
		}
		
		sb.append("<unfilled>" + unfilled.name() + "</unfilled>");
		
		if (usecredit) {
			sb.append("<usecredit/>");
		}
		if (nosplit) {
			sb.append("<nosplit/>");
		}
		if (expdate != null) {
			sb.append("<expdate>" + Utils.formatDate(expdate) + "</expdate>");
		}
		sb.append("</command>");
		return sb.toString();
	}

	public BuySell getBuysell() {
		return buysell;
	}

	public void setBuysell(BuySell buysell) {
		this.buysell = buysell;
	}

	public boolean isByMarket() {
		return byMarket;
	}

	public void setByMarket(boolean byMarket) {
		this.byMarket = byMarket;
	}

	public String getBrokerref() {
		return brokerref;
	}

	public void setBrokerref(String brokerref) {
		this.brokerref = brokerref;
	}

	public UnfilledType getUnfilled() {
		return unfilled;
	}

	public void setUnfilled(UnfilledType unfilled) {
		this.unfilled = unfilled;
	}

	public boolean isUsecredit() {
		return usecredit;
	}

	public void setUsecredit(boolean usecredit) {
		this.usecredit = usecredit;
	}

	public boolean isNosplit() {
		return nosplit;
	}

	public void setNosplit(boolean nosplit) {
		this.nosplit = nosplit;
	}

	public Date getExpdate() {
		return expdate;
	}

	public void setExpdate(Date expdate) {
		this.expdate = expdate;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}


}
