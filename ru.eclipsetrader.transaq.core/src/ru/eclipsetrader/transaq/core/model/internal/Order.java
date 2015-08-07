package ru.eclipsetrader.transaq.core.model.internal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.OrderConditionType;
import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.UnfilledType;
import ru.eclipsetrader.transaq.core.util.Utils;

@Table(name="orders")
@Entity
public class Order extends ServerObject {
	
	String transactionid;
	@Id
	String orderno;
	Integer secid;
	@Enumerated(EnumType.STRING)
	BoardType board;
	String seccode;
	String client;
	@Enumerated(EnumType.STRING)
	OrderStatus status;
	@Enumerated(EnumType.STRING)
	BuySell buysell;
	@Temporal(TemporalType.TIMESTAMP)
	Date time;
	@Temporal(TemporalType.TIMESTAMP)
	Date expdate;
	Long origin_orderno;
	@Temporal(TemporalType.TIMESTAMP)
	Date accepttime;
	String brokerref;
	double value;
	Double accruedint;
	String settlecode;
	int balance;
	Double price;
	int quantity;
	Integer hidden;
	double yield;
	@Temporal(TemporalType.TIMESTAMP)
	Date withdrawtime;
	String condition;
	double conditionvalue;
	@Temporal(TemporalType.TIMESTAMP)
	Date validafter;
	@Temporal(TemporalType.TIMESTAMP)
	Date validbefore;
	double maxcomission;
	String result;
	
	// NewOrder
	boolean bymarket;
	boolean nosplit = false;
	boolean usecredit = false;
	@Enumerated(EnumType.STRING)
	UnfilledType unfilled = UnfilledType.PutInQueue;
	
	// NewCondOrder
	@Enumerated(EnumType.STRING)
	OrderConditionType cond_type;
	Double cond_value;
	
	public Order() {
		super(null);
	}
	
	public Order(String serverId) {
		super(serverId);		
	}

	public String createNewCondOrder() {
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"newcondorder\">");
		sb.append("<security>");
		sb.append("<board>" + board + "</board>");
		sb.append("<seccode>" + seccode + "</seccode>");
		sb.append("</security>");
		sb.append("<client>" + client + "</client>");
		if (price != null) {
			sb.append("<price>" + price + "</price>");
		}
		if (hidden != null) {
			sb.append("<hidden>" + hidden + "</hidden>");
		}
		sb.append("<quantity>" + quantity + "</quantity>");
		sb.append("<buysell>" + buysell.name() + "</buysell>");
		if (bymarket) {
			sb.append("<bymarket/>");
		}
		if (brokerref != null) {
			sb.append("<brokerref>" + brokerref + "</brokerref>");
		}
		
		sb.append("<cond_type>" + cond_type + "</cond_type>");
		sb.append("<cond_value>" + cond_value + "</cond_value>");

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

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public Integer getSecid() {
		return secid;
	}

	public void setSecid(Integer secid) {
		this.secid = secid;
	}

	public BoardType getBoard() {
		return board;
	}

	public void setBoard(BoardType board) {
		this.board = board;
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BuySell getBuysell() {
		return buysell;
	}

	public void setBuysell(BuySell buysell) {
		this.buysell = buysell;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Date getExpdate() {
		return expdate;
	}

	public void setExpdate(Date expdate) {
		this.expdate = expdate;
	}

	public Long getOrigin_orderno() {
		return origin_orderno;
	}

	public void setOrigin_orderno(Long origin_orderno) {
		this.origin_orderno = origin_orderno;
	}

	public Date getAccepttime() {
		return accepttime;
	}

	public void setAccepttime(Date accepttime) {
		this.accepttime = accepttime;
	}

	public String getBrokerref() {
		return brokerref;
	}

	public void setBrokerref(String brokerref) {
		this.brokerref = brokerref;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Double getAccruedint() {
		return accruedint;
	}

	public void setAccruedint(Double accruedint) {
		this.accruedint = accruedint;
	}

	public String getSettlecode() {
		return settlecode;
	}

	public void setSettlecode(String settlecode) {
		this.settlecode = settlecode;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Integer getHidden() {
		return hidden;
	}

	public void setHidden(Integer hidden) {
		this.hidden = hidden;
	}

	public double getYield() {
		return yield;
	}

	public void setYield(double yield) {
		this.yield = yield;
	}

	public Date getWithdrawtime() {
		return withdrawtime;
	}

	public void setWithdrawtime(Date withdrawtime) {
		this.withdrawtime = withdrawtime;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public double getConditionvalue() {
		return conditionvalue;
	}

	public void setConditionvalue(double conditionvalue) {
		this.conditionvalue = conditionvalue;
	}

	public Date getValidafter() {
		return validafter;
	}

	public void setValidafter(Date validafter) {
		this.validafter = validafter;
	}

	public Date getValidbefore() {
		return validbefore;
	}

	public void setValidbefore(Date validbefore) {
		this.validbefore = validbefore;
	}

	public double getMaxcomission() {
		return maxcomission;
	}

	public void setMaxcomission(double maxcomission) {
		this.maxcomission = maxcomission;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean getBymarket() {
		return bymarket;
	}

	public void setBymarket(boolean bymarket) {
		this.bymarket = bymarket;
	}

	public boolean isNosplit() {
		return nosplit;
	}

	public void setNosplit(boolean nosplit) {
		this.nosplit = nosplit;
	}

	public boolean isUsecredit() {
		return usecredit;
	}

	public void setUsecredit(boolean usecredit) {
		this.usecredit = usecredit;
	}

	public UnfilledType getUnfilled() {
		return unfilled;
	}

	public void setUnfilled(UnfilledType unfilled) {
		this.unfilled = unfilled;
	}

	
}
