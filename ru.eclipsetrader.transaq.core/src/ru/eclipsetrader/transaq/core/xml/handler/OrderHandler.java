package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.exception.UnimplementedException;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.OrderStatus;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.StopLoss;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.TakeProfit;
import ru.eclipsetrader.transaq.core.util.Utils;

public class OrderHandler extends DefaultHandler {

	enum ProcessingType {
		ORDER, STOP_ORDER, STOP_LOSS, TAKE_PROFIT;
	}
	
	Event<Order> notifyOrder;
	Event<StopOrder> notifyStopOrder;
	
	Stack<String> elementStack = new Stack<String>();
	
	ProcessingType processingType;
	
	Order currentOrder = null;
	StopOrder currentStopOrder = null;
	
	public OrderHandler(Event<Order> notifyOrder, Event<StopOrder> notifyStopOrder) {
		this.notifyOrder = notifyOrder;
		this.notifyStopOrder = notifyStopOrder;
	}

	public void startElement(String qName, Attributes attributes) throws SAXException {
		elementStack.push(qName);
		String transactionId;
		switch (QNAME.valueOf(qName)) {
		case order:
			processingType = ProcessingType.ORDER;
			transactionId = attributes.getValue("transactionid");
			currentOrder = DataManager.getOrder(transactionId);
			if (currentOrder == null) {
				currentOrder = DataManager.merge(new Order(transactionId));
			}
			break;
			
		case stoporder:
			processingType = ProcessingType.STOP_ORDER;
			transactionId = attributes.getValue("transactionid");
			/* TODO refactor
			if (DataManager.hasStopOrder(transactionId)) {
				currentStopOrder = DataManager.getStopOrder(transactionId);
			} else {
				currentStopOrder = new StopOrder();
				DataManager.putStopOrder(transactionId, currentStopOrder);
			}
			break;*/
			throw new UnimplementedException();
		case stoploss:
			processingType = ProcessingType.STOP_LOSS;
			currentStopOrder.setStopLoss(new StopLoss());
			break;
			
		case takeprofit:
			processingType = ProcessingType.TAKE_PROFIT;
			currentStopOrder.setTakeProfit(new TakeProfit());
			break;
			
		default:
			break;
		}
	}

	public void endElement(String qName) throws SAXException {
		elementStack.pop();
		switch (QNAME.valueOf(qName)) {
		case order:
			currentOrder = DataManager.merge(currentOrder);
			if (notifyOrder != null) {
				notifyOrder.notifyObservers(currentOrder);
			}
			processingType = null;
			break;
			
		case stoporder:
			if (notifyStopOrder != null) {
				notifyStopOrder.notifyObservers(currentStopOrder);
			}
			/* TODO refactor
			currentStopOrder = DataManager.mergeStopOrder(currentStopOrder);
			processingType = null;
			break;
			*/			
			throw new UnimplementedException();
		case stoploss:
		case takeprofit:
			processingType = null;
			break;
		

		default:
			break;
		}
	}

	public void characters(String value) throws SAXException {
		String element = elementStack.peek();
		if (processingType != null) {
			switch (processingType) { 
			case ORDER :
				switch (QNAME.valueOf(element)) {
				case secid: currentOrder.setSecid(Integer.valueOf(value)); break;
				case orderno: currentOrder.setOrderno(Long.valueOf(value)); break;
				case board: 	currentOrder.setBoard(BoardType.valueOf(value)); break;
				case seccode:	currentOrder.setSeccode(value); break;
				case client:	currentOrder.setClient(value); break;
				case status:	currentOrder.setStatus(OrderStatus.valueOf(value)); break;
				case buysell:	currentOrder.setBuysell(BuySell.valueOf(value)); break;
				case time:	currentOrder.setTime(Utils.parseDate(value)); break;
				case expdate:	currentOrder.setExpdate(Utils.parseDate(value)); break;
				case origin_orderno:	currentOrder.setOrigin_orderno(Long.valueOf(value));break;
				case accepttime:		currentOrder.setAccepttime(Utils.parseDate(value));break;
				case brokerref:	currentOrder.setBrokerref(value); break;
				case value:	currentOrder.setValue(Double.valueOf(value));break;
				case accruedint:	currentOrder.setAccruedint(Double.valueOf(value)); break;
				case settlecode:	currentOrder.setSettlecode(value); break;
				case balance:	currentOrder.setBalance(Integer.valueOf(value)); break;
				case price:	currentOrder.setPrice(Double.valueOf(value)); break;
				case quantity: currentOrder.setQuantity(Integer.valueOf(value)); break;
				case hidden:	currentOrder.setHidden(Integer.valueOf(value)); break;
				case yield:	currentOrder.setYield(Double.valueOf(value)); break;
				case withdrawtime:	
					if (!"0".equals(value)) {
						currentOrder.setWithdrawtime(Utils.parseDate(value));
					} else {
						// TODO разобраться с датой
					}
					break;
				case condition:	currentOrder.setCondition(value);break;
				case conditionvalue:	currentOrder.setConditionvalue(Double.valueOf(value)); break;
				case validafter:	currentOrder.setValidafter(Utils.parseDate(value)); break;
				case validbefore:	currentOrder.setValidbefore(Utils.parseDate(value)); break;
				case maxcomission: currentOrder.setMaxcomission(Double.valueOf(value)); break;
				case result: currentOrder.setResult(value); break;
				default:
					break;
				}
				break;
				
			case STOP_ORDER:
				
				switch (QNAME.valueOf(element)) {
				case activeorderno:	currentStopOrder.setActiveorderno(value); break;
				case secid:	currentStopOrder.setSecid(Integer.valueOf(value)); break;
				case board:	currentStopOrder.setBoard(value); break;
				case seccode:	currentStopOrder.setSeccode(value); break;
				case client:	currentStopOrder.setClient(value); break;
				case buysell:	currentStopOrder.setBuysell(BuySell.valueOf(value)); break;
				case canceller:	currentStopOrder.setCanceller(value); break;
				case alltradeno: currentStopOrder.setAlltradeno(Long.valueOf(value)); break;
				case validbefore:	currentStopOrder.setValidbefore(Utils.parseDate(value)); break;
				case author:	currentStopOrder.setAuthor(value); break;
				case accepttime:	currentStopOrder.setAccepttime(Utils.parseDate(value)); break;
				case linkedorderno:	currentStopOrder.setLinkedorderno(Long.valueOf(value)); break;
				case expdate:	currentStopOrder.setExpdate(Utils.parseDate(value)); break;
				case status: currentStopOrder.setStatus(OrderStatus.valueOf(value)); break;
				default:
					break;
				}
				break;
				
			case STOP_LOSS:
				StopLoss stopLoss = currentStopOrder.getStopLoss();
				switch (QNAME.valueOf(element)) {
				case usecredit:  stopLoss.setUsecredit(value); break;
				case activationprice: stopLoss.setActivationprice(Double.valueOf(value)); break;
				case guardtime:	stopLoss.setGuardtime(Utils.parseDate(value)); break;
				case brokerref:	stopLoss.setBrokerref(value); break;
				case quantity:	stopLoss.setQuantity(Double.valueOf(value)); break;
				case orderprice:	stopLoss.setOrderprice(Double.valueOf(value)); break;
				default:
					break;
				}
				break;
				
			case TAKE_PROFIT:
				TakeProfit takeProfit = currentStopOrder.getTakeProfit();
				switch (QNAME.valueOf(element)) {
				case activationprice:	takeProfit.setActivationprice(Double.valueOf(value)); break;
				case guardtime:	takeProfit.setGuardtime(Utils.parseDate(value)); break;
				case brokerref:	takeProfit.setBrokerref(value); break;
				case quantity:	takeProfit.setQuantity(Double.valueOf(value)); break;
				case extremum:	takeProfit.setExtremum(Double.valueOf(value)); break;
				case level:		takeProfit.setLevel(Double.valueOf(value)); break;
				case correction:	takeProfit.setCorrection(Double.valueOf(value)); break;
				case guardspread:	takeProfit.setGuardspread(Double.valueOf(value)); break;
	
				default:
					break;
				}
				break;
			default:
				break;
			
			}
		}

	}

}
