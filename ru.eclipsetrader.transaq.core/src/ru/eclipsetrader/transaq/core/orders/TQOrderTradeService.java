package ru.eclipsetrader.transaq.core.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.services.ITQOrderTradeService;

public class TQOrderTradeService implements ITQOrderTradeService {

	private static long TIMEOUT = 1000 * 30;

	// список отправленных НА БИРЖУ запросов
	Map<String, OrderRequest> requests = Collections.synchronizedMap(new HashMap<String, OrderRequest>());
	// очередь для обработки принятых запросов
	BlockingQueue<Order> receivedOrders = new ArrayBlockingQueue<Order>(300);
	// очередь для обработки принятых сделок	
	BlockingQueue<Trade> receivedTrades = new ArrayBlockingQueue<Trade>(300);
	
	private Map<String, Order> orders = Collections.synchronizedMap(new HashMap<String, Order>());
	private Map<String, Trade> trades = Collections.synchronizedMap(new HashMap<String, Trade>());
	
	Thread ordersThread = new Thread(new OrderThreadProcessor());
	
	class OrderThreadProcessor implements Runnable {
		/**
		 * Implements order receive logic 
		 */
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					// получим новый ордер из очереди
					Order order = receivedOrders.take();
					
					// положим его в map
					put(order);
	
					// найдем его запрос
					OrderRequest orderRequest = requests.get(order.getTransactionid());
					
					if (orderRequest != null) {
						// оповестим, что пришел запрос
						orderRequest.notifyAll();
						// удалм запрос
						requests.remove(order.getTransactionid());
					} else {
						System.err.println("Order request for " + order.getTransactionid() + " NOT FOUND!");
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
	}
	
	Thread tradesThread = new Thread(new ThreadReceiveProcessor());
	
	class ThreadReceiveProcessor implements Runnable {

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				Trade trade;
				try {
					trade = receivedTrades.take();
					put(trade);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	Observer<Order> orderObserver = new Observer<Order>() {
		@Override
		public void update(Order order) {
			receivedOrders.add(order);
		}
	};
	
	Observer<Trade> tradeObserver = new Observer<Trade>() {
		@Override
		public void update(Trade trade) {
			receivedTrades.add(trade);
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
	
	private TQOrderTradeService() {
		ordersThread.setDaemon(true);
		ordersThread.start();
		tradesThread.setDaemon(true);
		tradesThread.start();
	}
	
	public void putOrderList(List<Order> orderList) {
		for (Order order : orderList) {
			put(order);
		}
	}
	
	public void put(Order order) {
		if (order.getTransactionid() == null || order.getTransactionid().isEmpty()) {
			throw new IllegalArgumentException("Cannot put order withour transactionId!");
		}
		orders.put(order.getTransactionid(), order);
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
	}

	@Override
	public List<Order> getOrders() {
		return new ArrayList<>(orders.values());
	}

	@Override
	public Order getOrderById(String transactionId) {
		return orders.get(transactionId);
	}
	
	@Override
	public Order getOrderByServerNo(Long orderNum) {
		for (Order order : orders.values()) {
			if (order.getOrderno() == orderNum) {
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
	public Order callNewOrder(OrderRequest orderRequest) {
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
	public String callCancelOrder(String transactionId) {
		System.out.println("Calling CancelOrder:\n" + transactionId);
		String command = "<command id=\"cancelorder\"><transactionid>" + transactionId +"</transactionid></command>";
		CommandResult result = TransaqLibrary.SendCommand(command);
		if (result.isSuccess()) {
			return result.getTransactionId();
		} else {
			throw new RuntimeException(result.getMessage());
		}		
	}
	
}
