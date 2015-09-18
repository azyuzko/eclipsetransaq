package com.investing;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class InvestingSignal {

	@Enumerated(EnumType.STRING)
	InvestingSymbol investingSymbol; // инструмент
	
	double price; // цена, по которой сработал сигнал
	
	@Temporal(TemporalType.TIMESTAMP)
	final LocalDateTime dateTime; // время создания сигнала
	
	int timeframe;  // таймфрейм
	@Enumerated(EnumType.STRING)
	InvestingState state;   // в какой состояние перейти

	boolean executed;	// был ли исполнен сигнал
	LocalDateTime executionDateTime; // время исполнения сигнала
	
	public InvestingSignal(InvestingSymbol investingSymbol, double price, int timeframe, InvestingState state) {
		this.investingSymbol = investingSymbol;
		this.price = price;
		this.timeframe = timeframe;
		this.state = state;
		this.dateTime = LocalDateTime.now();
	}

	public int getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(int timeframe) {
		this.timeframe = timeframe;
	}

	public InvestingState getToState() {
		return state;
	}

	public void setToState(InvestingState toState) {
		this.state = toState;
	}

	public InvestingSymbol getInvestingSymbol() {
		return investingSymbol;
	}

	public double getPrice() {
		return price;
	}


	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	

}
