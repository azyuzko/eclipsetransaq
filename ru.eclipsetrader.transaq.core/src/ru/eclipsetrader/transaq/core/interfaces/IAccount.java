package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IAccount {
	
	/**
	 * Свободные средства
	 * @return
	 */
	double getFree();
	
	/**
	 * Сбросить состояние счета на первоначальное
	 */
	void reset();
	
	/**
	 * Вернуть позицию
	 * @return
	 */
	int getPosition(TQSymbol symbol);
	
	/**
	 * Вернуть начальную позицию по счету
	 * @return
	 */
	int getInitialPosition(TQSymbol symbol);

	/**
	 * Купить контракты по рынку
	 * @param symbol
	 * @param quantity
	 * @return  кол-во и сумма реально купленных
	 */
	QuantityCost buy(TQSymbol symbol, int quantity);

	
	/**
	 * Продать контракты  по рынку
	 * @param symbol
	 * @param quantity
	 * @return кол-во и сумма реально проданных
	 */
	QuantityCost sell(TQSymbol symbol, int quantity);
	
	/**
	 * Закрыть позицию
	 * @param symbol
	 * @return
	 */
	QuantityCost close(TQSymbol symbol, double price);
	
}
