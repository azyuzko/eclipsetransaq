package ru.eclipsetrader.transaq.core.services;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * ����� ������ ��� ��������/�������
 * @author Zyuzko-AA
 *
 */
public interface ITQSubscription {
	
	void subscribe(TQSymbol symbol);
	void unsubscribe(TQSymbol symbol);
	void subscribe(TQSymbol[] symbols);
	void unsubscribe(TQSymbol[] symbols);
	void unsubscribeAll();
	
}
