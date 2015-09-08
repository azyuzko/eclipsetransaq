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
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.google.common.base.MoreObjects;

@MappedSuperclass
public abstract class Tick extends ServerObject implements ITQTickTrade {

	@Id
	String tradeno;

	@Embedded
	TQSymbol symbol = new TQSymbol();
	
	@Temporal(TemporalType.TIMESTAMP)
	Date time;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date received = new Date();
	
	double price;
	int quantity;
	@Enumerated(EnumType.STRING)
	BuySell buysell;
	@Enumerated(EnumType.STRING)
	TradePeriod period;
	int openinterest;
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.addValue(symbol)
			.addValue(tradeno)
			.addValue(Utils.formatDate(time))
			.addValue(buysell)
			.addValue(price)
			.addValue(quantity).toString();
	}
	
	public Tick() {
		this(null);
	}

	public Tick(String serverId) {
		super(serverId);
		if (TransaqServer.getInstance() != null) {
			received = TransaqServer.getInstance().getServerTime();
		}
	}

	
	public TQSymbol getSymbol() {
		return symbol;
	}
	
	public Date getReceived() {
		return received;
	}

	public Tick setReceived(Date received) {
		this.received = received;
		return this;
	}

	public String getTradeno() {
		return tradeno;
	}

	public Tick setTradeno(String tradeno) {
		this.tradeno = tradeno;
		return this;
	}

	public BoardType getBoard() {
		return symbol.getBoard();
	}

	public Tick setBoard(BoardType board) {
		this.symbol.setBoard(board);
		return this;
	}

	@Override
	public Date getTime() {
		return time;
	}

	public Tick setTime(Date time) {
		this.time = time;
		return this;
	}

	@Override
	public double getPrice() {
		return price;
	}

	public Tick setPrice(double price) {
		this.price = price;
		return this;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	public Tick setQuantity(int quantity) {
		this.quantity = quantity;
		return this;
	}

	public String getSeccode() {
		return symbol.getSeccode();
	}

	public Tick setSeccode(String seccode) {
		this.symbol.setSeccode(seccode);
		return this;
	}

	public BuySell getBuysell() {
		return buysell;
	}

	public Tick setBuysell(BuySell buysell) {
		this.buysell = buysell;
		return this;
	}

	public TradePeriod getPeriod() {
		return period;
	}

	public Tick setPeriod(TradePeriod period) {
		this.period = period;
		return this;
	}

	public int getOpeninterest() {
		return openinterest;
	}

	public Tick setOpeninterest(int openinterest) {
		this.openinterest = openinterest;
		return this;
	}

}
