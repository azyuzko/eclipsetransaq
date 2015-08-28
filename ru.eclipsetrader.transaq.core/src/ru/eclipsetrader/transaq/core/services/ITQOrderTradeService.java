package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.ICancelOrderCallback;
import ru.eclipsetrader.transaq.core.orders.ICreateOrderCallback;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;

public interface ITQOrderTradeService {

	List<Order> getOrders();
	List<Order> getActiveOrders();
	Order getOrderById(String transactionId);
	Order getOrderByServerNo(String orderno);
	
	Order createOrder(OrderRequest orderRequest);
	void createOrderAsync(OrderRequest orderRequest, ICreateOrderCallback callback);
	
	Order cancelOrder(String orderno);
	void cancelOrder(String orderno, ICancelOrderCallback callback);
	
	CommandResult moveOrder(String transactionId, double newPrice);
	CommandResult moveOrderQuantity(String transactionId, double newPrice, int quantity);
	StopOrder getStopOrderById(String transactionId);
	
	List<Trade> getTrades();
	Trade 		getTrade(String trandeNo);
	
	
}
