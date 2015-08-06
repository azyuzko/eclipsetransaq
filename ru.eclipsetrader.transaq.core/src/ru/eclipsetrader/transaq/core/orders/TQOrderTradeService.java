package ru.eclipsetrader.transaq.core.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.services.ITQOrderTradeService;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQOrderTradeService implements ITQOrderTradeService {

	Logger logger = LogManager.getLogger(TQOrderTradeService.class);
	
	private static long TIMEOUT = 1000 * 30;

	// список отправленных НА БИРЖУ запросов
	// ключ - transactionId
	Map<String, OrderRequest> requests = Collections.synchronizedMap(new HashMap<String, OrderRequest>());
	
	// ключ - orderno
	private Map<String, Order> orders = Collections.synchronizedMap(new HashMap<String, Order>());
	
	private Map<String, StopOrder> stopOrders = Collections.synchronizedMap(new HashMap<String, StopOrder>());
	
	// ключ - tradeno
	private Map<String, Trade> trades = Collections.synchronizedMap(new HashMap<String, Trade>());
	
	Observer<Order> orderObserver = new Observer<Order>() {
		@Override
		public void update(Order order) {
			logger.info("Received order " + order.getOrderno() + " transactionId = " + order.getTransactionid());

			if (logger.isTraceEnabled()) {
				logger.trace(Utils.toString(order));
			}
			
			if (order.getTransactionid() != null) {
				// найдем его запрос
					OrderRequest orderRequest = requests.get(order.getTransactionid());
				if (orderRequest != null) {
					// оповестим, что пришел запрос
					orderRequest.notifyAll();
					// удалим запрос
					requests.remove(order.getTransactionid());
				} else {
					logger.info("Order request for " + order.getTransactionid() + " not found. May be request from previous session");
				}
			} else {
				logger.info("Order " + order.getOrderno()  + " has no transactionId. May be order came from another Transaq server");
			}
			// положим запрос в map
			put(order);				
		}
	};
	
	Observer<Trade> tradeObserver = new Observer<Trade>() {
		@Override
		public void update(Trade trade) {
			logger.info("Received trade = " + trade.getTradeno() + " orderno = " + trade.getOrderno());
			put(trade);	
		}
	};
	
	Observer<StopOrder> stopOrderObserver = new Observer<StopOrder>() {
		
		@Override
		public void update(StopOrder stopOrder) {
			put(stopOrder);
		}
		
	};

	public Observer<Order> getOrderObserver() {
		return orderObserver;
	}
	
	public Observer<Trade> getTradeObserver() {
		return tradeObserver;
	}
	
	static TQOrderTradeService instance;
	public static TQOrderTradeService getInstance() {
		if (instance == null) {
			instance = new TQOrderTradeService();
		}
		return instance;
	}
	
	public void putOrderList(List<Order> orderList) {
		for (Order order : orderList) {
			put(order);
		}
	}
	
	public void put(Order order) {
		if (order.getTransactionid() == null) {
			// dont push orders without transactionId
			return;
		}
		orders.put(order.getOrderno(), order);
		DataManager.merge(order);
	}
	
	public void put(StopOrder stopOrder) {
		if (stopOrder.getTransactionid() == null) {
			// dont push orders without transactionId
			return;
		}
		stopOrders.put(stopOrder.getTransactionid(), stopOrder);
		DataManager.merge(stopOrder);
	}
	
	public void putTradeList(List<Trade> tradeList){
		for (Trade trade : tradeList) {
			put(trade);
		}
	}
	
	public void put(Trade trade) {
		if (trade.getTradeno() == null) {
			throw new IllegalArgumentException("Cannot put trade without tradeno!");
		}
		trades.put(trade.getTradeno(), trade);
		DataManager.merge(trade);
	}

	@Override
	public List<Order> getOrders() {
		return new ArrayList<>(orders.values());
	}
	
	@Override
	public List<Order> getActiveOrders() {
		List<Order> result = new ArrayList<Order>();
		for (Order order : orders.values()) {
			if (order.getStatus() == OrderStatus.active) {
				result.add(order);
			}
		}
		return result;
	}

	@Override
	public Order getOrderById(String transactionId) {
		return orders.get(transactionId);
	}
	
	@Override
	public Order getOrderByServerNo(String orderno) {
		for (Order order : orders.values()) {
			if (order.getOrderno().equals(orderno)) {
				return order;
			}
		}
		return null;
	}

	@Override
	public List<Trade> getTrades() {
		return new ArrayList<>(trades.values());
	}

	@Override
	public Trade getTrade(String trandeNo) {
		return trades.get(trandeNo);
	}

	@Override
	public Order createOrder(OrderRequest orderRequest) {
		logger.warn("Create new order:\n" + orderRequest);
		String command = orderRequest.createNewOrderCommand();
		CommandResult result = TransaqLibrary.SendCommand(command);
		if (result.getTransactionId() == null || result.getTransactionId().isEmpty()) {
			throw new RuntimeException("Null transactionId was returned");
		}
		requests.put(result.getTransactionId(), orderRequest);
		try {
			orderRequest.wait(TIMEOUT);
			return getOrderById(result.getTransactionId());
		} catch (InterruptedException e) {
			System.err.println("TIMEOUT EXCEPTION while waiting for order " + result.getTransactionId() +"from callback");
			throw new RuntimeException(e);
		}
	}

	@Override
	public String cancelOrder(String transactionId) {
		logger.warn("Perform order cancelling:" + transactionId);
		String command = "<command id=\"cancelorder\"><transactionid>" + transactionId +"</transactionid></command>";
		CommandResult result = TransaqLibrary.SendCommand(command);
		if (result.isSuccess()) {
			return result.getTransactionId();
		} else {
			throw new RuntimeException(result.getMessage());
		}		
	}

	@Override
	public StopOrder getStopOrderById(String transactionId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
