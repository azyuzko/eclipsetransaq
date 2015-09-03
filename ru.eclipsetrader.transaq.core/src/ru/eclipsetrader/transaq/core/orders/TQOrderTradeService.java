package ru.eclipsetrader.transaq.core.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQOrderTradeService implements ITQOrderTradeService {
	
	public enum RequestType {
		CancelOrder,
		MoveOrder
	}

	Logger logger = LogManager.getLogger(TQOrderTradeService.class);
	
	private static long TIMEOUT = 1000 * 30;

	// список отправленных НА БИРЖУ запросов на создание заявки
	// ключ - transactionId
	Map<String, Holder<OrderRequest, ICreateOrderCallback>> createRequests = Collections.synchronizedMap(new HashMap<String, Holder<OrderRequest, ICreateOrderCallback>>());

	// ключ - orderNo
	private Map<String, Order> orders = Collections.synchronizedMap(new HashMap<String, Order>());
	
	// ключ - transactionId
	private Map<String, StopOrder> stopOrders = Collections.synchronizedMap(new HashMap<String, StopOrder>());
	
	// ключ - tradeno
	private Map<String, Trade> trades = Collections.synchronizedMap(new HashMap<String, Trade>());
	
	public InstrumentEvent<Order> newOrderEvent = new InstrumentEvent<>("New Order Monitor Event");
	public InstrumentEvent<StopOrder> newStopOrderEvent = new InstrumentEvent<>("New StopOrder Monitor Event");
	public InstrumentEvent<Trade> newTradeEvent = new InstrumentEvent<>("New Trade Monitor Event");

	Observer<Order> orderObserver = new Observer<Order>() {
		@Override
		public void update(Order order) {
			String orderno = order.getOrderno();
			String transactionid = order.getTransactionid();
			logger.info("Received order " + order.getOrderDesc());
			if (transactionid != null) {
				// найдем его запрос
				 Holder<OrderRequest, ICreateOrderCallback> orderRequestHolder = createRequests.get(transactionid);
				if (orderRequestHolder != null) {
					// удалим запрос
					createRequests.remove(transactionid);
					// получим его callback
					ICreateOrderCallback callback = orderRequestHolder.getSecond();
					// оповестим
					if (order.getOrderno().equals("0")) {
						logger.warn("Received empty order : " + order.getOrderDesc());
						callback.onOrderCreateError(order, order.getResult());
					} else {
						callback.onOrderCreated(order);
						// посмотрим, есть ли у нас этот ордер
						Order currentOrder = orders.get(orderno);
						if (currentOrder != null) {
							logger.warn("Something wrong! currentOrder must be null here");
						}
						orders.put(orderno, order);
						DataManager.merge(order);
						return;
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
				DiffMap diff = currentOrder.notifyChanges(order);
				logger.info(("DIFF MAP = " + diff).replace("\n", ""));
				DataManager.merge(currentOrder);
			}
			
		}
	};
	
	Observer<Trade> tradeObserver = new Observer<Trade>() {
		@Override
		public void update(Trade trade) {
			logger.info("Received trade = " + trade.getTradeDesc());
			Objects.requireNonNull(trade.getTradeno(), "Cannot put trade without tradeno!");
			Trade old = trades.put(trade.getTradeno(), trade);
			if (old != null) {
				logger.warn("Something wrong! old trade must be null here");
			}
			DataManager.merge(trade);
		}
	};
	
	Observer<StopOrder> stopOrderObserver = new Observer<StopOrder>() {
		
		@Override
		public void update(StopOrder stopOrder) {
			logger.info("Received stop order " +  stopOrder.getStopOrderDesc());
			put(stopOrder);
			DataManager.merge(stopOrder);
		}
		
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
	public Order getOrderByServerNo(String orderno) {
		synchronized (orders) {
			for (Order order : orders.values()) {
				if (order.getOrderno().equals(orderno)) {
					return order;
				}
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
	public Order createOrder(final OrderRequest orderRequest) {
		synchronized (orderRequest) {
			logger.warn(orderRequest.getSymbol() + " Create new order "  + orderRequest.getBuysell());
			createOrderAsync(orderRequest, new OrderCallback() {
				@Override
				public void onOrderCreated(Order order) {
					synchronized (orderRequest) {
						super.onOrderCreated(order);
						logger.debug("received response order " + order.getOrderno());
						// положим его в запрос
						orderRequest.setOrder(order);
						// оповестим, что пришел ответ
						orderRequest.notifyAll();						
					}
				}
				@Override
				public void onOrderCreateError(Order order, String error) {
					synchronized (orderRequest) {
						super.onOrderCreateError(order, error);
						// положим его в запрос
						orderRequest.setOrder(order);
						// оповестим, что пришел ответ
						orderRequest.notifyAll();						
					}
				}
			});
			try {
				logger.debug("begin wait for server response");
				orderRequest.wait(TIMEOUT);
				return orderRequest.getOrder();
			} catch (InterruptedException e) {
				// не дождались ответа
				logger.error(e.getMessage(), e);
				if (orderRequest.getTransactionId() != null) {
					createRequests.remove(orderRequest.getTransactionId());
				}
				return null;
			}			
		}
		 
	}
	
	@Override
	public void createOrderAsync(OrderRequest orderRequest, ICreateOrderCallback callback) {
		String command = orderRequest.createNewOrderCommand();
		synchronized (orderRequest) {
			logger.warn(orderRequest.getSymbol() + " Create new "  + orderRequest.getBuysell() + " order: " + command);
			CommandResult result = TransaqLibrary.SendCommand(command);
			if (result.getTransactionId() == null || result.getTransactionId().isEmpty()) {
				callback.onTransaqError(result);
				return;
			}
			String transactionId = result.getTransactionId();
			orderRequest.setTransactionId(transactionId);
			createRequests.put(transactionId, new Holder<OrderRequest, ICreateOrderCallback>(orderRequest, callback));
		}
	}

	@Override
	public Order cancelOrder(String orderno) {
		Order order = orders.get(orderno);
		synchronized (order) {
			cancelOrder(orderno, new OrderCallback() {
				@Override
				public void onOrderCancelFailed(Order order) {
					super.onOrderCancelFailed(order);
					synchronized (order) {
						order.notifyAll();
					}
				}
				@Override
				public void onOrderCancelled(Order order) {
					super.onOrderCancelled(order);
					synchronized (order) {
						order.notifyAll();
					}
				}
				@Override
				public void onError(String message) {
					super.onError(message);
				}
				@Override
				public void onTransaqError(CommandResult commandResult) {
					super.onTransaqError(commandResult);
				}
			});
			try {
				order.wait(TIMEOUT);
				return order;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	@Override
	public void cancelOrder(String orderno, ICancelOrderCallback callback) {
		logger.info("Perform order cancelling:" + orderno);
		Order order = orders.get(orderno);
		if (order != null) {
			synchronized (order) {
				if (order.getCancelOrderCallback() != null) {
					callback.onError("Order " + orderno + "  already have active CANCEL request");
					return;
				}
				String command = "<command id=\"cancelorder\"><transactionid>" + order.getTransactionid() +"</transactionid></command>";
				CommandResult result = TransaqLibrary.SendCommand(command);
				if (!result.isSuccess()) {
					if (callback != null) {
						callback.onTransaqError(result);
					}
				} else {
					if (callback != null) {
						order.setCancelOrderCallback(callback);
					}
				}
			}			
		} else {
			if (callback != null) {
				callback.onError("Order not found = " + orderno);
			}
		}			

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
	
	@Override
	public void moveOrder(String orderno, double newPrice, int quantity, IMoveOrderCallback callback) {
		Order order = orders.get(orderno);
		if (order != null) {
			synchronized (order) {
				String command = 
					  "<command id=\"moveorder\">"
					+ "<transactionid>" + order.getTransactionid() + "</transactionid>"
					+ "<price>" + newPrice + "</price>"
					+ "<moveflag>1</moveflag>"
					+ "<quantity>" + quantity + "</quantity>"
					+ "</command>";
				CommandResult result = TransaqLibrary.SendCommand(command);
				if (!result.isSuccess()) {
					if (callback != null) {
						callback.onTransaqError(result);
					}
				} else {
					if (callback != null) {
						order.setMoveOrderCallback(callback);
					}
				}
			}
		} else {
			if (callback != null) {
				callback.onError("Order not found = " + orderno);
			}
		}
	}
	
	@Override
	public void moveOrder(String orderno, double newPrice, IMoveOrderCallback callback) {
		Order order = orders.get(orderno);
		if (order != null) {
			moveOrder(orderno, newPrice, order.getQuantity(), callback);
		} else {
			if (callback != null) {
				callback.onError("Order not found = " + orderno);
			}
		}
	}

	@Override
	public StopOrder getStopOrderById(String transactionId) {
		return stopOrders.get(transactionId);
	}
	
}
