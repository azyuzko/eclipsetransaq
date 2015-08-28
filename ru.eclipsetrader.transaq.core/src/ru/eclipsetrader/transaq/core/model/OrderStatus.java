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
	
	sl_executed,
	sl_guardtime,
	sl_forwarding,
	tp_executed,
	tp_guardtime,
	tp_forwarding,
	linkwait;
}
