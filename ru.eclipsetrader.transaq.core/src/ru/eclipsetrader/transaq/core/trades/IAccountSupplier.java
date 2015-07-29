package ru.eclipsetrader.transaq.core.trades;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * ��������� ������ �� �����
 * @author Zyuzko-AA
 *
 */
public interface IAccountSupplier {

	IAccount getAccount(TQSymbol symbol);
	
}
