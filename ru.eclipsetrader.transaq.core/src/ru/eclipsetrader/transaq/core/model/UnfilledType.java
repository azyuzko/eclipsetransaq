package ru.eclipsetrader.transaq.core.model;

public enum UnfilledType {
	/**
	 * ������������� ����� ������ ���������� � ������� ������ �����
	 */
	PutInQueue,
	
	/**
	 * ������ ����������� ������ � ��� ������, ���� ������ ����� ���� ������������� ���������
	 */
	CancelBalance,
	
	/**
	 * ������������� ����� ������ ��������� � ������
	 */
	ImmOrCancel;
}
