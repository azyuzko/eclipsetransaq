package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;

public class OvernightHandler extends BaseXMLProcessor<Boolean> {
	
	Boolean overnight = false;
	
	public OvernightHandler(Event<Boolean> overnightCallback) {
		super(overnightCallback);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		objectStack.push(getBoolean(attributes.getValue("status")));
	}

	@Override
	void endElement(String qName) throws SAXException {
		notifyCompleteElement(objectStack.pop());
	}

	@Override
	void characters(String value) throws SAXException {
		
	}
	
}
