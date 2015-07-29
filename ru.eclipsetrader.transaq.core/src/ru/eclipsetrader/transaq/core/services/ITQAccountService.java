package ru.eclipsetrader.transaq.core.services;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.interfaces.ITQPosition;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.TQSymbol;


public interface ITQAccountService extends IPersistable {

	public ITQPosition getPosition(ITQSecurity security);

	IAccount getAccount(TQSymbol symbol);
}
