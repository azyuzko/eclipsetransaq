package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;

public interface ITQOrderTradeService {

	List<Order> getOrders();
	Order getOrderById(String transactionId);
	Order getOrderByServerNo(Long orderNum);
	Order callNewOrder(OrderRequest orderRequest);
	
	List<Trade> getTrades();
	Trade 		getTrade(String trandeNo);
	
	String callCancelOrder(String transactionId);
}
