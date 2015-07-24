package ru.eclipsetrader.transaq.core.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;

@Embeddable
public class TQSymbol implements ITQKey, Serializable {

	public static final char DELIMITER_MARKET = '$';
	public static final char DELIMITER_BOARD = '#';
	private static final long serialVersionUID = 6098704782129781752L;

	String seccode;
	@Enumerated(EnumType.STRING)
	BoardType board;

	public TQSymbol() {
		// NullPointerException avoidance
		this.seccode = "";
	}
	
	public TQSymbol(BoardType boardType, String seccode) {
		this.board = boardType;
		this.seccode = seccode;
	}
	
	public TQSymbol(String board, String seccode) {
		this.board = BoardType.valueOf(board.toUpperCase());
		this.seccode = seccode;
	}
	
	public TQSymbol(TQSymbol symbol) {
		this.seccode = symbol.getSeccode();
		this.board = symbol.getBoard();
	}

	public String getSeccode() {
		return seccode;
	}

	public BoardType getBoard() {
		return board;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public void setBoard(BoardType board) {
		this.board = board;
	}

	public boolean equals(Object object) {
		if (object instanceof TQSymbol) {
			TQSymbol pk = (TQSymbol) object;
			return (seccode.equals(pk.seccode) && board.equals(pk.board));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return (seccode.hashCode() + board.hashCode());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (board != null) {
			sb.append(board);
			sb.append(DELIMITER_BOARD);
		}
		sb.append(seccode);
		return sb.toString();
	}

	@Override
	public String getKey() {
		return toString();
	}
	
	public static void main(String[] args) {

	}
}
