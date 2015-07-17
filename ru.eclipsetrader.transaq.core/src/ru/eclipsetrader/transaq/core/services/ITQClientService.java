package ru.eclipsetrader.transaq.core.services;

import ru.eclipsetrader.transaq.core.interfaces.ICustomStorage;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.internal.Client;

public interface ITQClientService extends ICustomStorage<String, Client> {

	void put(Client client);
	
	/**
	 * Возвращает ID клиента для инструмента
	 */
	String getSecurityClientId(ITQSecurity security);
}
