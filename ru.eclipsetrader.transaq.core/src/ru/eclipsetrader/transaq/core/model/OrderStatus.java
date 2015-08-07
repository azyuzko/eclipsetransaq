package ru.eclipsetrader.transaq.core.model;

public enum OrderStatus {
	none,
	active, // ��������
	cancelled, // ����� ��������� (������ ��� ������ �� ����� �	���� ��������)
	denied, // ��������� ��������
	disabled, // ���������� ��������� (�������� ������,	������� ����� �� ����������� �������)
	expired, // ����� �������� �������
	failed, // �� ������� ��������� �� �����
	forwarding, // ������������ �� �����
	inactive, // ������ �� �������� ��-�� ������� �� ������ � ������
	matched, // ���������
	refused, // ��������� ������������
	rejected, // ��������� ������
	removed, // ������������ ������
	wait, // �� ��������� ����� ���������
	watching, // ������� ����������� �������
	
	tp_executed, // ?????? 
	sl_executed;
}
