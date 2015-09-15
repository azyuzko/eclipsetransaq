package ru.eclipsetrader.transaq.core.helper;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.orders.OrderCallback;

public class MovementOrder {

	Order order;
	OrderCallback callback;
	
	public MovementOrder(Order order, OrderCallback callback) {
		this.order = order;
		this.callback = callback;
	}
	
	
}
