package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Map;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IAccount {
	
	/**
	 * ��������� ��������
	 * @return
	 */
	double getFree();
	
	/**
	 * �������� ��������� ����� �� ��������������
	 */
	void reset();
	
	/**
	 * ������� ������� �� �����
	 * @return
	 */
	Map<TQSymbol, QuantityCost> getPositions();
	
	/**
	 * ������� ��������� ������� �� �����
	 * @return
	 */
	Map<TQSymbol, QuantityCost> getInitialPositions();

	/**
	 * ������ ��������� �� �����
	 * @param symbol
	 * @param quantity
	 * @return  ���-�� � ����� ������� ���������
	 */
	QuantityCost buy(TQSymbol symbol, int quantity);

	
	/**
	 * ������� ���������  �� �����
	 * @param symbol
	 * @param quantity
	 * @return ���-�� � ����� ������� ���������
	 */
	QuantityCost sell(TQSymbol symbol, int quantity);
	
	/**
	 * ������� �������
	 * @param symbol
	 * @return
	 */
	QuantityCost close(TQSymbol symbol, double price);
	
}
