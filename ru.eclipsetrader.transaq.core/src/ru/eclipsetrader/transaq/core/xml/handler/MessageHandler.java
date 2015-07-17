package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.Message;
import ru.eclipsetrader.transaq.core.util.Utils;

public class MessageHandler extends BaseXMLProcessor<Message>{

	public MessageHandler(Event<Message> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		elementStack.push(qName);
		switch (QNAME.valueOf(qName)) {
		case message:
			Message message = new Message();
			objectStack.push(message);
			break;

		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {
		elementStack.pop();
		switch (QNAME.valueOf(qName)) {
		case message:
			notifyCompleteElement(objectStack.pop());
			break;

		default:
			break;
		}
	}

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {
		case date:
			Date time = Utils.parseTime(value);
			objectStack.peek().setDate(time);
			break;
		case text:
			objectStack.peek().setText(value);
			break;
		case from:
			objectStack.peek().setFrom(value);
			break;
		case urgent:
			objectStack.peek().setUrgent(getBoolean(value));
			break;

		default:
			break;
		}

	}

}
