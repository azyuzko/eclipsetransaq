package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;

public interface IExecuteOrderCallback {
	void onOrderExecute(Order order);
}
