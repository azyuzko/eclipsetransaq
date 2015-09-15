package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;

public interface ITransaqOrderOperationCallback extends ITransaqCallback {
	void onCompleteOperation(Order order);
}
