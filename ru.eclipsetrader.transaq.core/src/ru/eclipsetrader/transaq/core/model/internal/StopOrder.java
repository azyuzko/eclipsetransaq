package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.OrderStatus;

public class StopOrder  {

	String transactionid;
	String activeorderno;
	Integer secid;
	String board;
	String seccode;
	String client;
	BuySell buysell;
	String canceller;
	Long alltradeno;
	Date validbefore;
	String author;
	Date accepttime;
	Long linkedorderno;
	Date expdate;
	OrderStatus status;

	StopLoss stopLoss;
	TakeProfit takeProfit;

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	public String getActiveorderno() {
		return activeorderno;
	}

	public void setActiveorderno(String activeorderno) {
		this.activeorderno = activeorderno;
	}

	public Integer getSecid() {
		return secid;
	}

	public void setSecid(Integer secid) {
		this.secid = secid;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
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

	public BuySell getBuysell() {
		return buysell;
	}

	public void setBuysell(BuySell buysell) {
		this.buysell = buysell;
	}

	public String getCanceller() {
		return canceller;
	}

	public void setCanceller(String canceller) {
		this.canceller = canceller;
	}

	public Long getAlltradeno() {
		return alltradeno;
	}

	public void setAlltradeno(Long alltradeno) {
		this.alltradeno = alltradeno;
	}

	public Date getValidbefore() {
		return validbefore;
	}

	public void setValidbefore(Date validbefore) {
		this.validbefore = validbefore;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getAccepttime() {
		return accepttime;
	}

	public void setAccepttime(Date accepttime) {
		this.accepttime = accepttime;
	}

	public Long getLinkedorderno() {
		return linkedorderno;
	}

	public void setLinkedorderno(Long linkedorderno) {
		this.linkedorderno = linkedorderno;
	}

	public Date getExpdate() {
		return expdate;
	}

	public void setExpdate(Date expdate) {
		this.expdate = expdate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public StopLoss getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(StopLoss stopLoss) {
		this.stopLoss = stopLoss;
	}

	public TakeProfit getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(TakeProfit takeProfit) {
		this.takeProfit = takeProfit;
	}

}
