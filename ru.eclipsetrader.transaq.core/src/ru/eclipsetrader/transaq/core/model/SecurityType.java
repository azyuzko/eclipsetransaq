package ru.eclipsetrader.transaq.core.model;

public enum SecurityType {
	
	// ��������� �����������:
	SHARE, // - �����
	BOND, // - ��������� �������������
	FUT, // - �������� FORTS
	OPT, // - �������
	GKO, // - ���. ������
	FOB, // - �������� ����
	
	MCT,  
	ETS_CURRENCY,
	ETS_SWAP,

	// ����������� (��� ����� IDX �������� ������ � ���������� ��������):
	IDX, // - �������
	QUOTES, // - ��������� (������)
	CURRENCY, // - �������� ����
	ADR, // - ���
	NYSE, // - ������ � NYSE
	METAL, // - �������
	OIL, // - ��������
	
	/*SHA, // ��� ����� �������� � ����������!
	OP,
	SH,
	BO,
	RE,
	T,
	BON,
	ND,
	ARE,
	D,
	MC,
	FU,
	SHAR,
	E,
	ETS_CURRENC,
	Y,
	ID,
	X,
	ETS_SW,
	ETS_CU,
	AP,
	ETS_CURR,*/
}
