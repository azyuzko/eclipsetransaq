package com.investing;

public enum InvestingState {

	ACTIVESELL(-2),
	SELL(-1),
	NEUTRAL(0),
	BUY(1),
	ACTIVEBUY(2);
	
	int value;
	
	InvestingState(int value) {
		this.value = value;
	}
	
	public static InvestingState fromString(String hint) {
		switch (hint) {
		case "Активно продавать": return ACTIVESELL;
		case "Продавать": return SELL;
		case "Нейтрально": return NEUTRAL;
		case "Покупать": return BUY;
		case "Активно покупать": return ACTIVEBUY;		
		default:
			return null;
		}
	}
	
	public String desc() {
		switch (this) {
		case ACTIVESELL : return "Активно продавать";
		case SELL : return "Продавать";
		case NEUTRAL : return "Нейтрально";
		case BUY : return "Покупать";
		case ACTIVEBUY : return "Активно покупать";
		default:
			return "--";
		}
	}

	public int getValue() {
		return value;
	}
	
}
