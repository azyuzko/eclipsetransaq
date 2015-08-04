package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Map;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
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
	 * ������ ���������
	 * @param symbol
	 * @param quantity
	 * @return  ���-�� � ����� ������� ���������
	 */
	QuantityCost buy(TQSymbol symbol, int quantity, double price);

	/**
	 * ������ �� �������
	 * @param symbol
	 * @param quantity
	 * @param quoteGlass
	 * @return ���-�� � ����� ������� ���������
	 */
	QuantityCost buy(TQSymbol symbol, int quantity, QuoteGlass quoteGlass);
	
	/**
	 * ������� ���������
	 * @param symbol
	 * @param quantity
	 * @return ���-�� � ����� ������� ���������
	 */
	QuantityCost sell(TQSymbol symbol, int quantity, double price);
	
	/**
	 * ������� �� �������
	 * @param symbol
	 * @param quantity
	 * @param quoteGlass
	 * @return ���-�� � ����� ������� ���������
	 */
	QuantityCost sell(TQSymbol symbol, int quantity, QuoteGlass quoteGlass);
	
	/**
	 * ������� �������
	 * @param symbol
	 * @return
	 */
	QuantityCost close(TQSymbol symbol, double price);
	
}
