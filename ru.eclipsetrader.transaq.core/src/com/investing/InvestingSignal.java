package com.investing;

import java.util.ArrayList;
import java.util.List;

import ru.eclipsetrader.transaq.core.model.internal.Trade;


public class InvestingSignal {
	String code;
	
	int timeframe;
	InvestingState fromState;
	InvestingState toState;
	
	List<Trade> trades = new ArrayList<Trade>();
	
	public InvestingSignal(int timeframe, InvestingState fromState, InvestingState toState, String desc) {
		this.timeframe = timeframe;
		this.fromState = fromState;
		this.toState = toState;
		this.code = desc;
	}

	public int getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(int timeframe) {
		this.timeframe = timeframe;
	}

	public InvestingState getFromState() {
		return fromState;
	}

	public void setFromState(InvestingState fromState) {
		this.fromState = fromState;
	}

	public InvestingState getToState() {
		return toState;
	}

	public void setToState(InvestingState toState) {
		this.toState = toState;
	}

	public String getDesc() {
		return code;
	}

	public void setDesc(String desc) {
		this.code = desc;
	}

}
