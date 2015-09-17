package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.OrderCallback;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;

public interface ITQOrderTradeService {

	List<Order> getOrders();
	List<Order> getActiveOrders();
	Order getOrderById(String transactionId);
	Order getOrderByServerNo(String orderno);
	
	Order createOrder(OrderRequest orderRequest, OrderCallback callback);
	void cancelOrder(String orderno);
	Order moveOrder(String orderno, double newPrice);
	
	StopOrder getStopOrderById(String transactionId);
	
	List<Trade> getTrades();
	Trade 		getTrade(String trandeNo);
	
	
}
