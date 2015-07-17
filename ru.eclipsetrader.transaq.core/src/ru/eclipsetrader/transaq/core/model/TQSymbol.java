package ru.eclipsetrader.transaq.core.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;
import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;

@Embeddable
public class TQSymbol implements ITQSymbol, ITQKey, Serializable {

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
	
	public TQSymbol(ITQSymbol symbol) {
		this.seccode = symbol.getSeccode();
		this.board = symbol.getBoard();
	}

	@Override
	public String getSeccode() {
		return seccode;
	}

	@Override
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
		} else if (object instanceof ITQSymbol) {
			ITQSymbol pk = (ITQSymbol) object;
			return (seccode.equals(pk.getSeccode()) && board.equals(pk.getBoard()));
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
	
	public static String symbolKey(ITQSymbol symbol) {
		return (new TQSymbol(symbol)).getKey();
	}
	
	public static void main(String[] args) {
		TQSymbol s1 = new TQSymbol(BoardType.FUT, "BRN5");
		TQSymbol s2 = new TQSymbol("FUT", new String("BRN5"));
		ITQSymbol s = new ITQSymbol() {
			
			@Override
			public String getSeccode() {
				return new String("BRN5");
			}
			
			@Override
			public BoardType getBoard() {
				return BoardType.FUT;
			}
		};
		TQSymbol s3 = new TQSymbol(s);
		
		System.out.println(s1.equals(s2));
		System.out.println(s2.equals(s3));
		System.out.println(s3.equals(s1));
		System.out.println(s3.equals(s2));
		System.out.println(s.equals(s1));
		System.out.println(s1.equals(s));
		
		System.out.println(s1.hashCode());
		System.out.println(s2.hashCode());
		System.out.println(s3.hashCode());
		System.out.println(s.hashCode());
	}
}
