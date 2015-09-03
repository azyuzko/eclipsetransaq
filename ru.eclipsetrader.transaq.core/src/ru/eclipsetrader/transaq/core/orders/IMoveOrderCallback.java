package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;

public interface IMoveOrderCallback extends ITransaqCallback {

	void onOrderMoved(Order order);
	void onOrderMoveFailed(Order order);
	
}
