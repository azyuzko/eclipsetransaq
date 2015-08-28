package ru.eclipsetrader.transaq.core.interfaces;

import java.util.Map;

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
	 * Вернуть позиции по счету
	 * @return
	 */
	Map<TQSymbol, QuantityCost> getPositions();
	
	/**
	 * Вернуть начальные позиции по счету
	 * @return
	 */
	Map<TQSymbol, QuantityCost> getInitialPositions();

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
