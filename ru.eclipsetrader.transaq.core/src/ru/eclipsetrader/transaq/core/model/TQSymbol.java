package ru.eclipsetrader.transaq.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;

@Embeddable
public class TQSymbol implements ITQKey, Serializable {
	
	public static TQSymbol BRQ5 = new TQSymbol(BoardType.FUT, "BRQ5"); // BR-8.15
	
	public static TQSymbol BRU5 = new TQSymbol(BoardType.FUT, "BRU5"); // BR-9.15
	public static TQSymbol SiU5 = new TQSymbol(BoardType.FUT, "SiU5"); // Si-9.15
	public static TQSymbol EuU5 = new TQSymbol(BoardType.FUT, "EuU5"); // Eu-9.15
	public static TQSymbol EDU5 = new TQSymbol(BoardType.FUT, "EDU5"); // ED-9.15
	public static TQSymbol RIU5 = new TQSymbol(BoardType.FUT, "RIU5"); // RTS-9.15
	public static TQSymbol SPU5 = new TQSymbol(BoardType.FUT, "SPU5"); // SBPR-9.15
	public static TQSymbol SRU5 = new TQSymbol(BoardType.FUT, "SRU5"); // SBRF-9.15
	public static TQSymbol VBU5 = new TQSymbol(BoardType.FUT, "VBU5"); // VTBR-9.15
	public static TQSymbol GZU5 = new TQSymbol(BoardType.FUT, "GZU5"); // GAZR-9.15

	public static TQSymbol BRV5 = new TQSymbol(BoardType.FUT, "BRV5"); // BR-10.15

	public static TQSymbol USD000UTSTOD = new TQSymbol(BoardType.CETS, "USD000UTSTOD"); // USD Tod
	public static TQSymbol USD000UTSTOM = new TQSymbol(BoardType.CETS, "USD000UTSTOM"); // USD Tom
	public static TQSymbol USD000TODTOM = new TQSymbol(BoardType.CETS, "USD000TODTOM"); // USD Tod/Tom
	public static TQSymbol EUR_RUB__TOD = new TQSymbol(BoardType.CETS, "EUR_RUB__TOD"); // 
	public static TQSymbol EUR_RUB__TOM = new TQSymbol(BoardType.CETS, "EUR_RUB__TOM"); //
	public static TQSymbol EUR000TODTOM = new TQSymbol(BoardType.CETS, "EUR000TODTOM"); //
	
	public static TQSymbol VTBR = new TQSymbol(BoardType.TQBR, "VTBR");
	public static TQSymbol GAZP = new TQSymbol(BoardType.TQBR, "GAZP");
	public static TQSymbol SBER = new TQSymbol(BoardType.TQBR, "SBER");
	
	public static List<TQSymbol> workingSymbolSet() {
		ArrayList<TQSymbol> result = new ArrayList<TQSymbol>();
		result.add(TQSymbol.BRQ5);
		result.add(TQSymbol.BRU5);
		result.add(TQSymbol.SiU5);
		result.add(TQSymbol.EuU5);
		result.add(TQSymbol.EDU5);
		result.add(TQSymbol.RIU5);
		result.add(TQSymbol.SPU5);
		result.add(TQSymbol.SRU5);
		result.add(TQSymbol.VBU5);
		result.add(TQSymbol.GZU5);
		result.add(TQSymbol.USD000UTSTOD);
		result.add(TQSymbol.USD000UTSTOM);
		result.add(TQSymbol.USD000TODTOM);
		result.add(TQSymbol.EUR_RUB__TOD);
		result.add(TQSymbol.EUR_RUB__TOM);
		result.add(TQSymbol.EUR000TODTOM);
		return result;
	}

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
