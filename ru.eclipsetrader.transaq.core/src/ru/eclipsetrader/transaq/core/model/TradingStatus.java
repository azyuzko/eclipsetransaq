package ru.eclipsetrader.transaq.core.model;

public enum TradingStatus {
	N, // ���������� ��� ������ (����)
	O, // ������ �������� (����)
	�, // ����� ������� (����)
	F, // ������ �������� (����)
	B, // ������� (����)
	T, // �������� ������ (����)
	L, // ������ �������������� �������� (����)
	E, // ������ ������ �� ���� �������� ��������
	D, // ������� �������� ��������
	I, // ���������� �������
	
	S, // ����������
	
	_0, // ������ ���������. ������ ������� ������, �� ����� ������� (FORTS)
	_1, // ������ ����. ����� ������� � ������� ������ (FORTS)
	_2, // ������������ ������ �� ���� ������������.	������ ������� ������, �� ����� �������	(FORTS)
	_3, // ������ ������������� ���������. ������ ������� � ������� ������ (FORTS)
	_4 // ������ ��������� �� �������. ������ ������� �	������� ������ (FORTS)
}
