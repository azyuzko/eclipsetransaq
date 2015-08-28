package ru.eclipsetrader.transaq.core.orders;

import java.util.Map;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.util.Holder;

public interface IOrderCallback {

	void onOrderChangeStatus(Order order, Map<String, Holder<Object, Object>> diff);
	void onOrderUpdate(Order order, Map<String, Holder<Object, Object>> diff);
	
}
