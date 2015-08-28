package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;

public interface ICancelOrderCallback extends ITransaqCallback {
	void onOrderCancelled(Order order);
	void onOrderCancelFailed(Order order);
}
