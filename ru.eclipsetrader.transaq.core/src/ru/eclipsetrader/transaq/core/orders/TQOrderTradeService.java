package ru.eclipsetrader.transaq.core.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.services.ITQOrderTradeService;

public class TQOrderTradeService implements ITQOrderTradeService {

	Logger logger = LogManager.getLogger(TQOrderTradeService.class);
	
	private static long TIMEOUT = 1000 * 30;

	// список отправленных НА БИРЖУ запросов на создание заявки
	// ключ - transactionId
	Map<OrderRequest, OrderCallback> createRequests = Collections.synchronizedMap(new WeakHashMap<OrderRequest, OrderCallback>());
	
	Map<MoveOrderRequest, OrderCallback> moveOrderRequests = Collections.synchronizedMap(new WeakHashMap<MoveOrderRequest, OrderCallback>());

	// ключ - orderNo
	private Map<String, Order> orders = Collections.synchronizedMap(new HashMap<String, Order>());
	
	// ключ - transactionId
	private Map<String, StopOrder> stopOrders = Collections.synchronizedMap(new HashMap<String, StopOrder>());
	
	// ключ - tradeno
	private Map<String, Trade> trades = Collections.synchronizedMap(new HashMap<String, Trade>());
	
	public InstrumentEvent<Order> newOrderEvent = new InstrumentEvent<>("New Order Monitor Event");
	public InstrumentEvent<StopOrder> newStopOrderEvent = new InstrumentEvent<>("New StopOrder Monitor Event");
	public InstrumentEvent<Trade> newTradeEvent = new InstrumentEvent<>("New Trade Monitor Event");

	Observer<Order> orderObserver = (Order order) -> {
		String orderno = order.getOrderno();
		String transactionid = order.getTransactionid();
		logger.info("Received order " + order.getOrderDesc());
		if (transactionid != null) {
			// найдем его запрос
			Optional<OrderRequest> orderRequestOptional = createRequests.keySet().stream().filter(or -> or.getTransactionId().equals(transactionid)).findFirst();
			if (orderRequestOptional.isPresent()) {
				OrderRequest orderRequest = orderRequestOptional.get();
				// получим его callback и удалим
				OrderCallback callback = createRequests.remove(orderRequest);
				// оповестим
				if (order.getOrderno().equals("0") || order.getStatus() == OrderStatus.cancelled) {
					logger.info("Order cancelled : " + order.getOrderDesc());
					callback.onCreateError(orderRequest, order, order.getResult());
				} else {
					// посмотрим, есть ли у нас этот ордер
					Order currentOrder = orders.get(orderno);
					if (currentOrder != null) {
						logger.warn("Something wrong! currentOrder must be null here");
					}
					orders.put(orderno, order);
					orderRequest.setOrder(order);
					callback.onCreated(orderRequest, order);
					DataManager.merge(order);
					return;
				}
			} else {
				Optional<MoveOrderRequest> moveOrderOptional = moveOrderRequests.keySet().stream().filter(or -> or.getNewPrice() == order.getPrice()).findFirst();
				if (moveOrderOptional.isPresent()) {
					MoveOrderRequest moveOrderRequest = moveOrderOptional.get();
					moveOrderRequest.movedOrder = order;
					moveOrderRequests.remove(moveOrderRequests).onOrderMoved(moveOrderRequest.getOrder(), order);
				}
			}
		}
		
		// посмотрим, есть ли у нас этот ордер
		Order currentOrder = orders.get(orderno);
		
		if (currentOrder == null) {
			// такого ордера нет, оповестим заинтересованных
			orders.put(orderno, order);
			DataManager.merge(order);
			newOrderEvent.notifyObservers(order.getSymbol(), order);
		} else {
			DiffMap diff = currentOrder.merge(order);
			DataManager.merge(currentOrder);
		}
		
	};
	
	Observer<Trade> tradeObserver = (Trade trade) -> {
		logger.info("Received trade = " + trade.getTradeDesc());
		Objects.requireNonNull(trade.getTradeno(), "Cannot put trade without tradeno!");
		Trade old = trades.put(trade.getTradeno(), trade);
		if (old != null) {
			logger.warn("Something wrong! old trade must be null here");
		}
		newTradeEvent.notifyObservers(trade.getSymbol(), trade);
		DataManager.merge(trade);
	};
	
	Observer<StopOrder> stopOrderObserver = (StopOrder stopOrder) -> {
		logger.info("Received stop order " +  stopOrder.getStopOrderDesc());
		put(stopOrder);
		DataManager.merge(stopOrder);
	};

	public Observer<Order> getOrderObserver() {
		return orderObserver;
	}
	
	public Observer<Trade> getTradeObserver() {
		return tradeObserver;
	}
	
	public Observer<StopOrder> getStopOrderObserver() {
		return stopOrderObserver;
	}
	
	static TQOrderTradeService instance;
	public static TQOrderTradeService getInstance() {
		if (instance == null) {
			instance = new TQOrderTradeService();
		}
		return instance;
	}
	
	public StopOrder put(StopOrder stopOrder) {
		if (stopOrder.getTransactionid() == null) {
			// dont push orders without transactionId
			return null;
		}
		StopOrder old = stopOrders.put(stopOrder.getTransactionid(), stopOrder);
		DataManager.merge(stopOrder);
		return old;
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
	public synchronized Order getOrderByServerNo(String orderno) {
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
	public Order createOrder(OrderRequest orderRequest, OrderCallback callback) {
		synchronized (orderRequest) {
			if (orderRequest.getOrder() != null) {
				throw new RuntimeException("OrderRequest <" + orderRequest.toString() + "> already has created order <" + orderRequest.getOrder().getOrderDesc() + ">");
			}
			String command = orderRequest.createNewOrderCommand();
			logger.info(orderRequest.getSymbol() + " Create new "  + orderRequest.getBuysell() + " order: " + command);						
			CommandResult result = TransaqLibrary.SendCommand(command);
			if (result.getTransactionId() == null || result.getTransactionId().isEmpty()) {
				orderRequest.setErrorMessage(result.getMessage());
				callback.onTransaqError(result);
				return null;
			}
			String transactionId = result.getTransactionId();
			orderRequest.setTransactionId(transactionId);
			createRequests.put(orderRequest, new OrderCallback() {
				@Override
				public void onCreated(OrderRequest orderRequest, Order order) {
					callback.onCreated(orderRequest, order);
					synchronized (orderRequest) {
						orderRequest.notify();
					}
				}
				@Override
				public void onCreateError(OrderRequest orderRequest, Order order, String error) {
					orderRequest.setErrorMessage(error);
					callback.onCreateError(orderRequest, order, error);
					synchronized (orderRequest) {
						orderRequest.notify();
					}
				}
			});
			
			synchronized (orderRequest) {
				try {
					orderRequest.wait(TIMEOUT);
					return orderRequest.getOrder();
				} catch (InterruptedException e) {
					e.printStackTrace();
					createRequests.remove(orderRequest);
					callback.onCreateError(orderRequest, null, e.getMessage());
					return null;
				}
			}
			
		}
	}

	@Override
	public void cancelOrder(String orderno) {
		logger.info("Perform order cancelling:" + orderno);
		Order order = orders.get(orderno);
		if (order != null) {
			if (order.tryLock()) {
				try {
					String command = "<command id=\"cancelorder\"><transactionid>" + order.getTransactionid() +"</transactionid></command>";
					CommandResult result = TransaqLibrary.SendCommand(command);
					if (!result.isSuccess()) {
						order.onTransaqError(result);
					}
				} finally {
					order.unlock();
				}
			} else {
				logger.warn("cancelOrder failed! Order " + orderno + " already locked for another operation");
			}
		} else {
			throw new RuntimeException("Order not found = " + orderno);
		}			

	}

	@Override
	public Order moveOrder(String orderno, double newPrice) {
		Order order = orders.get(orderno);
		if (order != null) {
			if (order.tryLock()) {
				try {
					if (order.getStatus() != OrderStatus.active) {
						System.err.println("Cannot move order with status = " + order.getStatus());
						return null;
					}
					MoveOrderRequest moveOrderRequest = new MoveOrderRequest(order, newPrice, 0); 
					String command = moveOrderRequest.createRequest();
					CommandResult result = TransaqLibrary.SendCommand(command);
					if (!result.isSuccess()) {
						order.onTransaqError(result);
					}
					moveOrderRequests.put(moveOrderRequest, new OrderCallback() {
						@Override
						public void onOrderMoved(Order oldOrder, Order newOrder) {
							synchronized (moveOrderRequest) {
								moveOrderRequest.notify();
							}
						}
					});
					synchronized (moveOrderRequest) {
						try {
							moveOrderRequest.wait(TIMEOUT);
							return moveOrderRequest.getMovedOrder();
						} catch (InterruptedException e) {
							e.printStackTrace();
							moveOrderRequests.remove(moveOrderRequest);
							return null;
						}
					}
					
				} finally {
					order.unlock();
				}
			} else {
				throw new RuntimeException("moveOrder failed! Order " + orderno + " already locked for another operation");
			}
		} else {
			throw new RuntimeException("Order not found = " + orderno);
		}
	}

	@Override
	public StopOrder getStopOrderById(String transactionId) {
		return stopOrders.get(transactionId);
	}
	
}
