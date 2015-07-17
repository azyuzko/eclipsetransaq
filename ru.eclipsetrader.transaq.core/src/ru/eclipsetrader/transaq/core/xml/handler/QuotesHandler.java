package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;

public class QuotesHandler extends BaseXMLProcessor<SymbolGapMap> {

	public QuotesHandler(ListEvent<SymbolGapMap> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {

		elementStack.push(qName);

		switch (QNAME.valueOf(qName)) {
		
		case quotes:
			tempList.clear();
			break;
		
		case quote:
			SymbolGapMap map = new SymbolGapMap();
			objectStack.push(map);
			break;

		default:
			break;
		}

	}

	@Override
	void endElement(String qName) throws SAXException {
		
		elementStack.pop();

		switch (QNAME.valueOf(qName)) {
		case quotes:
			notifyCompleteProcess();
			break;
			
		case quote:
			tempList.add(objectStack.pop());
			break;

		default:
			break;
		}

	}

	@Override
	void characters(String value) throws SAXException {

		switch (QNAME.valueOf(currentElement())) {
		// do nothing
		case quotes:
		case quote:
			break;
			
		case seccode:
			objectStack.peek().setSeccode(value);
			break;
		case board:
			objectStack.peek().setBoard(BoardType.valueOf(value));
			break;

		default:
			objectStack.peek().put(currentElement(), value);
			break;

		}
	}

}
