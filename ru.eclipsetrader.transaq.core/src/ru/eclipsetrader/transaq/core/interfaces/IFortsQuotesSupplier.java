package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * Поставщик данных по котировкам для счета ФОРТС
 * @author Zyuzko-AA
 *
 */
public interface IFortsQuotesSupplier {
	
	double getBuyPrice(TQSymbol symbol);
	double getSellPrice(TQSymbol symbol);
	double getPrice(TQSymbol symbol);

}
