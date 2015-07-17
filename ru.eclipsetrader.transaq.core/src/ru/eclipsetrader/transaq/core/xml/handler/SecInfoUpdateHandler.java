package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.internal.SecInfoUpdate;

public class SecInfoUpdateHandler extends BaseXMLProcessor<SecInfoUpdate> {

	public SecInfoUpdateHandler(Event<SecInfoUpdate> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		elementStack.push(qName);
		switch (QNAME.valueOf(qName)) {
		case sec_info_upd:
			SecInfoUpdate secInfoUpdate = new SecInfoUpdate();
			objectStack.push(secInfoUpdate);
			break;

		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {
		elementStack.pop();
		switch (QNAME.valueOf(qName)) {
		case sec_info_upd:
			notifyCompleteElement(objectStack.pop());
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
		case market:
			objectStack.peek().setMarket(Integer.valueOf(value));
			break;
		case seccode:
			objectStack.peek().setSeccode(value);
			break;
		case minprice:
			objectStack.peek().setMinprice(Double.valueOf(value));
			break;
		case maxprice:
			objectStack.peek().setMaxprice(Double.valueOf(value));
			break;
		case buy_deposit:
			objectStack.peek().setBuy_deposit(Double.valueOf(value));
			break;
		case sell_deposit:
			objectStack.peek().setSell_deposit(Double.valueOf(value));
			break;
		case bgo_c:
			objectStack.peek().setBgo_c(Double.valueOf(value));
			break;
		case bgo_nc:
			objectStack.peek().setBgo_nc(Double.valueOf(value));
			break;
		case bgo_buy:
			objectStack.peek().setBgo_buy(Double.valueOf(value));
			break;
		case point_cost:
			objectStack.peek().setMinprice(Double.valueOf(value));
			break;

		default:
			break;
		}

	}

}
