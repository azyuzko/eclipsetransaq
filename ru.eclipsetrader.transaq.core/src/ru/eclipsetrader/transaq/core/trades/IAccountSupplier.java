package ru.eclipsetrader.transaq.core.trades;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * Поставщик данных по счету
 * @author Zyuzko-AA
 *
 */
public interface IAccountSupplier {

	IAccount getAccount(TQSymbol symbol);
	
}
