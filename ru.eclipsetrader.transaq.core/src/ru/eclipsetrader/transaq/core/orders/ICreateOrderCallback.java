package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;

public interface ICreateOrderCallback extends ITransaqCallback {

	void onOrderCreated(Order order);
	void onOrderCreateError(Order order, String error);

}
