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
		case "������� ���������": return ACTIVESELL;
		case "���������": return SELL;
		case "����������": return NEUTRAL;
		case "��������": return BUY;
		case "������� ��������": return ACTIVEBUY;		
		default:
			return null;
		}
	}
	
	public String desc() {
		switch (this) {
		case ACTIVESELL : return "������� ���������";
		case SELL : return "���������";
		case NEUTRAL : return "����������";
		case BUY : return "��������";
		case ACTIVEBUY : return "������� ��������";
		default:
			return "--";
		}
	}

	public int getValue() {
		return value;
	}
	
}
