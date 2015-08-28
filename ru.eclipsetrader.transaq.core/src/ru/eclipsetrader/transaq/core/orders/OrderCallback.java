package ru.eclipsetrader.transaq.core.orders;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.util.Holder;

public class OrderCallback implements IOrderCallback, ICreateOrderCallback, ICancelOrderCallback, IExecuteOrderCallback {
	
	Logger logger = LogManager.getLogger("OrderCallback");

	public void onTransaqError(CommandResult commandResult) {
		logger.error(commandResult);
	}
	
	@Override
	public void onError(String message) {
		logger.error(message);
	}
	
	public void onOrderCreated(Order order) {
		logger.info("Order registered " + order.getOrderDesc());
	}
	
	public void onOrderCreateError(Order order, String error) {
		logger.error("Order error <" + error + "> " + order.getOrderDesc());
	}
	
	public void onOrderChangeStatus(Order order, Map<String, Holder<Object, Object>> diff) {
		logger.info("Order changed status " + order.getOrderDesc());
	}
	
	public void onOrderUpdate(Order order, Map<String, Holder<Object, Object>> diff) {
		logger.info("Order updated " + order.getOrderDesc());
	}
	
	public void onOrderExecute(Order order) {
		logger.info("Order " + order.getOrderDesc() +" EXECUTED");
	}

	@Override
	public void onOrderCancelled(Order order) {
		logger.info("Order cancelled " + order.getOrderDesc());
	}

	@Override
	public void onOrderCancelFailed(Order order) {
		logger.info("Order cancel FAILED " + order.getOrderDesc());
	}

	
}
