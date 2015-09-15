package ru.eclipsetrader.transaq.core.orders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;

public class OrderCallback{
	
	Logger logger = LogManager.getLogger("OrderCallback");

	public void onTransaqError(CommandResult commandResult) {
		logger.error(commandResult);
	}
	
	public void onError(String message) {
		logger.error(message);
	}

	public void onChangeState(Order order, OrderStatus oldState, OrderStatus newState, DiffMap diff) {
		logger.info("Order changed status from <" + oldState + "> to <" + newState + ">" + order.getOrderDesc());
	}

	public void onCreated(OrderRequest orderRequest, Order order) {
		logger.info("Order created " + order.getOrderDesc());
	}

	public void onCreateError(OrderRequest orderRequest, Order order, String error) {
		logger.info("Create error <" + error + "> on " + order.getOrderDesc());
	}
	
	public void onExecuted(Order order) {
		logger.info("EXECUTED " + order.getOrderDesc());
	}

	
}
