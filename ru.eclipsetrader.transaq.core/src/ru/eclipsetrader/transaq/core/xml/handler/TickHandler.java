package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TradePeriod;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TickHandler extends BaseXMLProcessor<TickTrade> {

	public TickHandler(ListEvent<TickTrade> onAllTradeChange) {
		super(onAllTradeChange);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		
		elementStack.push(qName);

		switch (QNAME.valueOf(qName)) {
		case alltrades:
		case ticks:
			tempList = new ArrayList<TickTrade>();
			break;
			
		case tick:
		case trade:
			TickTrade tickTrade = new TickTrade();
			objectStack.push(tickTrade);
			break;

		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {

		elementStack.pop();
		
		switch (QNAME.valueOf(qName)) {
		case tick:
		case trade:
			tempList.add(objectStack.pop());
			break;
		case alltrades:
		case ticks:			
			notifyCompleteProcess();
			break;
		default:
			break;
		}

	}

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {
		case secid:
			objectStack.peek().setSecid(Integer.valueOf(value));
			break;
		case tradeno:
			objectStack.peek().setTradeno(value);
			break;
		case time:			
		case tradetime:
			objectStack.peek().setTime(Utils.parseDate(value));
			break;
		case price:
			objectStack.peek().setPrice(Double.valueOf(value));
			break;
		case quantity:
			objectStack.peek().setQuantity(Integer.valueOf(value));
			break;
		case period:
			objectStack.peek().setPeriod(TradePeriod.valueOf(value));
			break;
		case buysell:
			objectStack.peek().setBuysell(BuySell.valueOf(value));
			break;
		case openinterest:
			objectStack.peek().setOpeninterest(Integer.valueOf(value));
			break;
		case board:
			objectStack.peek().setBoard(BoardType.valueOf(value));
			break;
		case seccode:
			objectStack.peek().setSeccode(value);
			break;

		default:
			break;
		}

	}

}
