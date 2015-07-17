package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.interfaces.ITQTickTrade;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradePeriod;

@MappedSuperclass
public abstract class Tick extends ServerObject implements ITQTickTrade {

	@Id
	String tradeno;
	Integer secid;

	@Embedded
	TQSymbol symbol = new TQSymbol();
	
	@Temporal(TemporalType.TIMESTAMP)
	Date time;
	double price;
	int quantity;
	@Enumerated(EnumType.STRING)
	BuySell buysell;
	@Enumerated(EnumType.STRING)
	TradePeriod period;
	int openinterest;
	
	public Tick() {
		this(null);
	}

	public Tick(String serverId) {
		super(serverId);
	}

	public Integer getSecid() {
		return secid;
	}

	public void setSecid(Integer secid) {
		this.secid = secid;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public BoardType getBoard() {
		return symbol.getBoard();
	}

	public void setBoard(BoardType board) {
		this.symbol.setBoard(board);
	}

	@Override
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getSeccode() {
		return symbol.getSeccode();
	}

	public void setSeccode(String seccode) {
		this.symbol.setSeccode(seccode);
	}

	public BuySell getBuysell() {
		return buysell;
	}

	public void setBuysell(BuySell buysell) {
		this.buysell = buysell;
	}

	public TradePeriod getPeriod() {
		return period;
	}

	public void setPeriod(TradePeriod period) {
		this.period = period;
	}

	public int getOpeninterest() {
		return openinterest;
	}

	public void setOpeninterest(int openinterest) {
		this.openinterest = openinterest;
	}

}
