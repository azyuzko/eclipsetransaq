package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.internal.SecInfo;

public class SecInfoHandler extends BaseXMLProcessor<SecInfo> {

	public SecInfoHandler(Event<SecInfo> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		elementStack.push(qName);
		switch (QNAME.valueOf(qName)) {
		case sec_info:
			
			break;

		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {
		elementStack.pop();
	}

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {

		default:
			break;
		}
	}

}
