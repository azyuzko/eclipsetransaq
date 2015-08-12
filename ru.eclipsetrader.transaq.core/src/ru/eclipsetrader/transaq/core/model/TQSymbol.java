package ru.eclipsetrader.transaq.core.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;

@Embeddable
public class TQSymbol implements ITQKey, Serializable {
	
	public static TQSymbol BRQ5 = new TQSymbol(BoardType.FUT, "BRQ5"); // BR-8.15
	
	public static TQSymbol _1BQ5 = new TQSymbol(BoardType.FUT, "1BQ5"); // IBVS-8.15 на Индекс BOVESPA
	public static TQSymbol _2BQ5 = new TQSymbol(BoardType.FUT, "2BQ5"); // SNSX-8.15 на Индекс SENSEX
	public static TQSymbol _3BQ5 = new TQSymbol(BoardType.FUT, "3BQ5"); // HSIF-8.15 на Индекс Hang Seng
	
	public static TQSymbol BRU5 = new TQSymbol(BoardType.FUT, "BRU5"); // BR-9.15
	public static TQSymbol SiU5 = new TQSymbol(BoardType.FUT, "SiU5"); // Si-9.15
	public static TQSymbol EuU5 = new TQSymbol(BoardType.FUT, "EuU5"); // Eu-9.15
	public static TQSymbol EDU5 = new TQSymbol(BoardType.FUT, "EDU5"); // ED-9.15
	
	public static TQSymbol RIU5 = new TQSymbol(BoardType.FUT, "RIU5"); // RTS-9.15
	public static TQSymbol RSU5 = new TQSymbol(BoardType.FUT, "RSU5"); // RTSS-9.15
	public static TQSymbol VIQ5 = new TQSymbol(BoardType.FUT, "VIQ5"); // RVI-8.15 Фьючерсный контракт на волатильность российского рынка
	public static TQSymbol MXU5 = new TQSymbol(BoardType.FUT, "MXU5"); // MIX-9.15	
	public static TQSymbol _3BU5 = new TQSymbol(BoardType.FUT, "3BU5"); // HSIF-9.15 на Индекс Hang Seng
	public static TQSymbol _4BU5 = new TQSymbol(BoardType.FUT, "4BU5"); // ALSI-9.15 на Индекс FTSE/JSE Top40
	
	public static TQSymbol SPU5 = new TQSymbol(BoardType.FUT, "SPU5"); // SBPR-9.15
	public static TQSymbol SRU5 = new TQSymbol(BoardType.FUT, "SRU5"); // SBRF-9.15
	public static TQSymbol VBU5 = new TQSymbol(BoardType.FUT, "VBU5"); // VTBR-9.15
	public static TQSymbol GZU5 = new TQSymbol(BoardType.FUT, "GZU5"); // GAZR-9.15
	public static TQSymbol GDU5 = new TQSymbol(BoardType.FUT, "GDU5"); // GOLD-9.15
	public static TQSymbol SVU5 = new TQSymbol(BoardType.FUT, "SVU5"); // SILV-9.15
	public static TQSymbol AUU5 = new TQSymbol(BoardType.FUT, "AUU5"); // AUDU-9.15
	
	public static TQSymbol BRV5 = new TQSymbol(BoardType.FUT, "BRV5"); // BR-10.15

//	public static TQSymbol USD000000TOD = new TQSymbol(BoardType.CETS, "USD000000TOD"); // USD Tod
//	public static TQSymbol USD000UTSTOM = new TQSymbol(BoardType.CETS, "USD000UTSTOM"); // USD Tom
//	public static TQSymbol EUR_RUB__TOD = new TQSymbol(BoardType.CETS, "EUR_RUB__TOD"); // 
//	public static TQSymbol EUR_RUB__TOM = new TQSymbol(BoardType.CETS, "EUR_RUB__TOM"); //
//	public static TQSymbol USD000TODTOM = new TQSymbol(BoardType.CETS, "USD000TODTOM"); // USD Tod/Tom
//	public static TQSymbol EUR000TODTOM = new TQSymbol(BoardType.CETS, "EUR000TODTOM"); //
	
//	public static TQSymbol VTBR = new TQSymbol(BoardType.TQBR, "VTBR");
//	public static TQSymbol GAZP = new TQSymbol(BoardType.TQBR, "GAZP");
//	public static TQSymbol SBER = new TQSymbol(BoardType.TQBR, "SBER");
//	public static TQSymbol LKOH = new TQSymbol(BoardType.TQBR, "LKOH");
//	public static TQSymbol ROSN = new TQSymbol(BoardType.TQBR, "ROSN");

	public static TQSymbol RTSI = new TQSymbol(BoardType.INDEXR, "RTSI");
	public static TQSymbol RTS2 = new TQSymbol(BoardType.INDEXR, "RTS2");
	//public static TQSymbol MICEX = new TQSymbol(BoardType.INDEXR, "MICEX");
	
	public static List<TQSymbol> workingSymbolSet() {
		ArrayList<TQSymbol> result = new ArrayList<TQSymbol>();
		for (Field f : TQSymbol.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(TQSymbol.class) ) {
				try {
					TQSymbol s = (TQSymbol)f.get(null);
					result.add(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		for (TQSymbol s : TQSymbol.workingSymbolSet()) {
			System.out.println(s);
		}
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

}
