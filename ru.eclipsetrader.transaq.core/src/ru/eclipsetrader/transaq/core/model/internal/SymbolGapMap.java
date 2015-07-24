package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;
import java.util.HashMap;

import ru.eclipsetrader.transaq.core.model.BoardType;

/**
 * Gap для котировки по бумаге
 * @author Zyuzko-AA
 *
 */
public class SymbolGapMap extends HashMap<String, String> {

	private static final long serialVersionUID = 1250896457729641149L;
	
	BoardType board;
	String seccode;
	Date time;
	
	public SymbolGapMap(Date time) {
		this.time = time;
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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	

}
