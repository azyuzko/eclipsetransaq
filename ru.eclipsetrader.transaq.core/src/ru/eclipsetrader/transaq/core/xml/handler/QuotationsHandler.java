package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;

public class QuotationsHandler extends BaseXMLProcessor<SymbolGapMap> {

	public QuotationsHandler(Event<SymbolGapMap> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		
		elementStack.push(qName);
		
		switch (QNAME.valueOf(qName)) {
		case quotation:
			SymbolGapMap gapMap = new SymbolGapMap();
			objectStack.add(gapMap);
			break;
		default:
			break;
		}
		
	}

	@Override
	void endElement(String qName) throws SAXException {
		
		elementStack.pop();
		
		switch (QNAME.valueOf(qName)) {
		case quotation:
			notifyCompleteElement(objectStack.pop());
			break;
		default:
			break;
		}
	}

	@Override
	void characters(String value) throws SAXException {
		switch (QNAME.valueOf(currentElement())) {
		
		// do nothing
		case quotations:
		case quotation:
			break;
			
		case board:
			objectStack.peek().setBoard(BoardType.valueOf(value));
			break;
			
		case seccode:
			objectStack.peek().setSeccode(value);
			break;

		default:
			objectStack.peek().put(currentElement(), value);
			break;
		}
		

	}

}
