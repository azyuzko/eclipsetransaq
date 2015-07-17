package ru.eclipsetrader.transaq.core.model.internal;
/*
 * 0 - ������ ������ ��� (��������� �� ���)
 * 1 - ���������� ���������� ������ (���� ���� ��� - ������ ��� ������)
 * 2 - ����������� ������� (����� ��� ������)
 * 3 - ��������� ������ ���������� (���� ����� ����������� ��������� �����)
 */
public enum CandleStatus {

	NO_MORE(0),
	FULL_PROVIDED(1),
	HAS_MORE_DATA(2),
	NOT_AVAILABLE(3);
	
	private int value;
	private CandleStatus(int i){
		this.value = i;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public static CandleStatus getFromValue(String value){
		switch (Integer.valueOf(value)) {
		case 0:
			return NO_MORE;
		case 1:
			return FULL_PROVIDED;
		case 2:
			return HAS_MORE_DATA;
		case 3:
			return NOT_AVAILABLE;
		default:
			throw new IllegalArgumentException("Unknown value: " + value);
		}
	}
}
