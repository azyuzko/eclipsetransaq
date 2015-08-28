package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
@Table(name="StopOrders")
public class StopOrder  {

	String activeorderno;
	@Id
	String transactionid;
	@Enumerated(EnumType.STRING)
	BoardType board;
	String seccode;
	String client;
	@Enumerated(EnumType.STRING)
	BuySell buysell;
	String canceller;
	Long alltradeno;
	@Temporal(TemporalType.TIMESTAMP)
	Date validbefore;
	String author;
	@Temporal(TemporalType.TIMESTAMP)
	Date accepttime;
	Long linkedorderno;
	@Temporal(TemporalType.TIMESTAMP)
	Date expdate;
	@Enumerated(EnumType.STRING)
	OrderStatus status;

	@Embedded
	StopLoss stopLoss;
	@Embedded
	TakeProfit takeProfit;
	
	public StopOrder() {
		
	}
	
	public StopOrder(String transactionId) {
		this.transactionid = transactionId;
	}
	
	public TQSymbol getSymbol() {
		return new TQSymbol(board, seccode);
	}
	
	@Override
	public String toString() {
		return Utils.toString(this);
	}

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
