package ru.eclipsetrader.transaq.core.orders;

import java.util.Date;

import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.UnfilledType;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.util.Utils;

public class OrderRequest {

	String transactionId;
	
	TQSymbol symbol;
	double price;
	int quantity;
	int hidden;
	BuySell buysell;
	boolean byMarket = false;
	String brokerref;
	private UnfilledType unfilled = UnfilledType.PutInQueue; // немедленно, или отменить ImmOrCancel не работает для FUT
	private boolean usecredit;
	private boolean nosplit;
	private Date expdate;
	
	private Order order; // созданный по запросу ордер
	
	private OrderRequest(TQSymbol symbol, BuySell bs, int quantity) {
		this.symbol = symbol;
		this.quantity = quantity;
		this.buysell = bs;
	}
	
	@Override
	public String toString() {
		return symbol + " " + buysell + " " + price + " " + quantity;
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
	
	public static void main(String[] args) {
		OrderRequest o = new OrderRequest(TQSymbol.RIZ5, BuySell.B, 1);
		o.setPrice(48.57);
		System.out.println(o.createNewOrderCommand());
		
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public OrderRequest setSymbol(TQSymbol symbol) {
		this.symbol = symbol;
		return this;
	}

	public int getHidden() {
		return hidden;
	}

	public OrderRequest setHidden(int hidden) {
		this.hidden = hidden;
		return this;		
	}

	public double getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public String createNewOrderCommand() {
/*		Security securty =  TQSecurityService.getInstance().get(symbol);
		if (securty != null && securty.getMinStep() == 0) {
			throw new RuntimeException("Unable to create order for " + securty + ", minStep = 0,  Security is readonly" );
		}*/
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"neworder\">");
		sb.append("<security>");
		sb.append("<board>" + symbol.getBoard() + "</board>");
		sb.append("<seccode>" + symbol.getSeccode() + "</seccode>");
		sb.append("</security>");
		sb.append("<client>" + TQClientService.getInstance().getSecurityClientId(symbol) + "</client>");
		if (price != 0) {
			double rest = Math.abs( price - Math.rint(price) );
			if (rest < 0.00001) {
				sb.append("<price>" + Math.round(price)  + "</price>");
			} else {
				sb.append("<price>" + price + "</price>");
			}
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

	public OrderRequest setBuysell(BuySell buysell) {
		this.buysell = buysell;
		return this;		
	}

	public boolean isByMarket() {
		return byMarket;
	}

	public OrderRequest setByMarket(boolean byMarket) {
		this.byMarket = byMarket;
		return this;		
	}

	public String getBrokerref() {
		return brokerref;
	}

	public OrderRequest setBrokerref(String brokerref) {
		this.brokerref = brokerref;
		return this;		
	}

	public UnfilledType getUnfilled() {
		return unfilled;
	}

	public OrderRequest setUnfilled(UnfilledType unfilled) {
		this.unfilled = unfilled;
		return this;		
	}

	public boolean isUsecredit() {
		return usecredit;
	}

	public OrderRequest setUsecredit(boolean usecredit) {
		this.usecredit = usecredit;
		return this;		
	}

	public boolean isNosplit() {
		return nosplit;
	}

	public OrderRequest setNosplit(boolean nosplit) {
		this.nosplit = nosplit;
		return this;		
	}

	public Date getExpdate() {
		return expdate;
	}

	public OrderRequest setExpdate(Date expdate) {
		this.expdate = expdate;
		return this;		
	}

	public OrderRequest setPrice(double price) {
		this.price = price;
		return this;		
	}

	public OrderRequest setQuantity(int quantity) {
		this.quantity = quantity;
		return this;		
	}


}
