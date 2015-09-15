package com.investing;

import java.util.TreeMap;

public enum InvestingSymbol {
	
	EURUSD(1),
	GBPUSD(2),
	USDJPY(3),
	AUDUSD(5),
	EURJPY(9),
	EURRUB(1691),
	USDRUB(2186),
	BRENT(8833),
	WTI(8849),
	RTS(13665),
	MICEX(13666),
	GOLD(8830),
	SILVER(8836),
	PALLADIUM(8883),
	CUPRUM(8831),
	PLATINUM(8910)
	;
	
	int code;
	InvestingSymbol(int code) {
		this.code = code;
	}
	
	static TreeMap<Integer, InvestingSymbol> enumStorage = new TreeMap<Integer, InvestingSymbol>();
	static {
		for (InvestingSymbol is : InvestingSymbol.values()) {
			enumStorage.put(is.code, is);
		}
	}
	
	public int getCode() {
		return this.code;
	}
	
	public static InvestingSymbol getInvSymbol(int code) {
		return enumStorage.get(code);
	}
}
