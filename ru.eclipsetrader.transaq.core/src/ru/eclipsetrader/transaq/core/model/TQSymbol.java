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
	
	public static TQSymbol BRV5 = new TQSymbol(BoardType.FUT, "BRV5"); // BR-10.15
	public static TQSymbol SiZ5 = new TQSymbol(BoardType.FUT, "SiZ5"); // Si-12.15
	public static TQSymbol EuZ5 = new TQSymbol(BoardType.FUT, "EuZ5"); // Eu-12.15
	public static TQSymbol EDZ5 = new TQSymbol(BoardType.FUT, "EDZ5"); // ED-12.15
	
	public static TQSymbol RIZ5 = new TQSymbol(BoardType.FUT, "RIZ5"); // RTS-12.15
	public static TQSymbol MXZ5 = new TQSymbol(BoardType.FUT, "MMZ5"); // MXI-12.15	

	public static TQSymbol GDZ5 = new TQSymbol(BoardType.FUT, "GDZ5"); // GOLD-12.15
	public static TQSymbol SVZ5 = new TQSymbol(BoardType.FUT, "SVZ5"); // SILV-12.15

	//public static TQSymbol RSU5 = new TQSymbol(BoardType.FUT, "RSZ5"); // RTSS-9.15
	//public static TQSymbol VIQ5 = new TQSymbol(BoardType.FUT, "VIZ5"); // RVI-9.15 Фьючерсный контракт на волатильность российского рынка

	//public static TQSymbol _1BU5 = new TQSymbol(BoardType.FUT, "1BU5"); // IBVS-8.15 на Индекс BOVESPA
	//public static TQSymbol _2BU5 = new TQSymbol(BoardType.FUT, "2BU5"); // SNSX-8.15 на Индекс SENSEX
	//public static TQSymbol _3BU5 = new TQSymbol(BoardType.FUT, "3BU5"); // HSIF-9.15 на Индекс Hang Seng
	//public static TQSymbol _4BU5 = new TQSymbol(BoardType.FUT, "4BU5"); // ALSI-9.15 на Индекс FTSE/JSE Top40
	
	//public static TQSymbol SPU5 = new TQSymbol(BoardType.FUT, "SPU5"); // SBPR-9.15
	//public static TQSymbol SRU5 = new TQSymbol(BoardType.FUT, "SRU5"); // SBRF-9.15
	//public static TQSymbol VBU5 = new TQSymbol(BoardType.FUT, "VBU5"); // VTBR-9.15
	//public static TQSymbol GZU5 = new TQSymbol(BoardType.FUT, "GZU5"); // GAZR-9.15
	
	

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

//	public static TQSymbol RTSI = new TQSymbol(BoardType.INDEXR, "RTSI");
//	public static TQSymbol RTS2 = new TQSymbol(BoardType.INDEXR, "RTS2");
//	public static TQSymbol MIX = new TQSymbol(BoardType.INDEXR, "MIX");
	
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
