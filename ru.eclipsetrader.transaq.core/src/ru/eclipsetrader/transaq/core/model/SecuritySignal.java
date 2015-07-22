package ru.eclipsetrader.transaq.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.data.BooleanConverter;
import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.model.internal.ServerObject;
import ru.eclipsetrader.transaq.core.util.Utils;


@Entity
@EntityListeners(DefaultJPAListener.class)
public class SecuritySignal extends ServerObject {

	@Id
	@GeneratedValue(generator="seq_signal", strategy=GenerationType.SEQUENCE)
	@SequenceGenerator(name="seq_signal", sequenceName="seq_signal", allocationSize=10)
	Long id;
	
	String strategy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "signaltime")
	Date time;
	
	String instrument;

	@Enumerated(EnumType.STRING)
	BuySell buySell;

	int quantity;
	double price;

	@Convert(converter = BooleanConverter.class)
	boolean byMarket;
	
	String log; 
	
	public String toString() {
		return Utils.toString(this);
	}
	
	public SecuritySignal(String serverId) {
		super(serverId);
		this.time = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public BuySell getBuySell() {
		return buySell;
	}

	public void setBuySell(BuySell buySell) {
		this.buySell = buySell;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isByMarket() {
		return byMarket;
	}

	public void setByMarket(boolean byMarket) {
		this.byMarket = byMarket;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
	
	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	
}
