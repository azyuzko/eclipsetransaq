package ru.eclipsetrader.transaq.core.xml.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TradePeriod;
import ru.eclipsetrader.transaq.core.model.internal.Trade;


public class TradeHandler extends BaseXMLProcessor<Trade> {
	
	private static Logger logger = LogManager.getLogger(TradeHandler.class);

	public TradeHandler(Event<Trade> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		
		elementStack.push(qName);
		
		switch (QNAME.valueOf(qName)) {
		case trade:
			Trade trade = new Trade();
			objectStack.push(trade);
			break;

		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {

		elementStack.pop();

		switch (QNAME.valueOf(qName)) {
		case trade:
			notifyCompleteElement(objectStack.pop());
			break;

		default:
			break;
		}
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {
		case tradeno:
			objectStack.peek().setTradeno(value);
			break;
		case board:
			objectStack.peek().setBoard(BoardType.valueOf(value));
			break;
		case time:
			try {
				objectStack.peek().setTime(sdf.parse(value));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case price:
			objectStack.peek().setPrice(Double.valueOf(value));
			break;
		case quantity:
			objectStack.peek().setQuantity(Integer.valueOf(value));
			break;
		case buysell:
			objectStack.peek().setBuysell(BuySell.valueOf(value));
			break;
		case seccode:
			objectStack.peek().setSeccode(value);
			break;
		case period:
			objectStack.peek().setPeriod(TradePeriod.valueOf(value));
			break;
		case orderno:
			objectStack.peek().setOrderno(value);
			break;
		case client:
			objectStack.peek().setClient(value);
			break;
		case brokerref:
			objectStack.peek().setBrokerref(value);
			break;
		case value:
			objectStack.peek().setValue(Double.valueOf(value));
			break;
		case items:
			objectStack.peek().setItems(Integer.valueOf(value));
			break;
		case comission:
			objectStack.peek().setComission(Double.valueOf(value));
			break;
		case yield:
			objectStack.peek().setYield(Double.valueOf(value));
			break;
		case accruedint:
			objectStack.peek().setAccruedint(Double.valueOf(value));
			break;
		case tradetype:
			objectStack.peek().setTradetype(value);
			break;
		case settlecode:
			objectStack.peek().setSeccode(value);
			break;
		case currentpos:
			objectStack.peek().setCurrentpos(Double.valueOf(value));
			break;
		
		case trade:
		case trades:
			break;			
			
		default:
			logger.warn("Undefined trade attribute " + currentElement() + " value:\n" +value);
			break;
		}
	}


}
