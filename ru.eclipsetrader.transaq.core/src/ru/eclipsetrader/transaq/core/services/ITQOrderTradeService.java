package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;

public interface ITQOrderTradeService {

	List<Order> getOrders();
	List<Order> getActiveOrders();
	Order getOrderById(String transactionId);
	Order getOrderByServerNo(String orderno);
	Order createOrder(OrderRequest orderRequest);
	String cancelOrder(String transactionId);
	
	List<Trade> getTrades();
	Trade 		getTrade(String trandeNo);
	
	
}
