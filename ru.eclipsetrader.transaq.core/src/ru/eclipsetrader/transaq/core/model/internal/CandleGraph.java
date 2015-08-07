package ru.eclipsetrader.transaq.core.model.internal;

import java.util.ArrayList;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.Candle;

public class CandleGraph {

	String board;
	String seccode;
	Integer period;
	CandleStatus status;
	
	List<Candle> candles  = new ArrayList<Candle>();

	public String getKey() {
		return board+"_"+seccode;
	}
	
	public List<Candle> getCandles() {
		return candles;
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

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public CandleStatus getStatus() {
		return status;
	}

	public void setStatus(CandleStatus status) {
		this.status = status;
	}


}
