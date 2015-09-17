package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.Order;


public class MoveOrderRequest {

	double newPrice;
	int quantity;
	Order order;
	Order movedOrder;

	public MoveOrderRequest(Order order, double newPrice, int quantity) {
		this.newPrice = newPrice;
		this.quantity = quantity;
	}
	
	/**
	 * Передвинуть ордер
	 * moveflag = 
	 * 0: не менять количество;
	 * 1: изменить количество;
	 * 2: при несовпадении количества с текущим – снять заявку.
	 * @param transactionId
	 * @param newPrice
	 * @param quantity
	 * @return
	 */
	public String createRequest() {
		String command = 
				  "<command id=\"moveorder\">"
				+ "<transactionid>" + order.getTransactionid() + "</transactionid>"
				+ "<price>" + newPrice + "</price>"
				+ (quantity > 0 ?
					"<moveflag>1</moveflag>"
				  + "<quantity>" + quantity + "</quantity>" : "<moveflag>0</moveflag>")
				+ "</command>";
		return command;
	}

	public double getNewPrice() {
		return newPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public Order getOrder() {
		return order;
	}

	public Order getMovedOrder() {
		return movedOrder;
	}

}
