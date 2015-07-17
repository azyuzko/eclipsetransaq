package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;

public class ErrorHandler extends BaseXMLProcessor<String> {

	public ErrorHandler(Event<String> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		elementStack.push(qName);
	}

	@Override
	void endElement(String qName) throws SAXException {
		elementStack.pop();
		switch (QNAME.valueOf(qName)) {
		case error:
			notifyCompleteElement(objectStack.pop());
			break;

		default:
			break;
		}
	}

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {
		case error:
			objectStack.push(value);
			break;

		default:
			break;
		}

	}

}
