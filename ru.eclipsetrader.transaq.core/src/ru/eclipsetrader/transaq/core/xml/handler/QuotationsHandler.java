package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;

public class QuotationsHandler extends BaseXMLProcessor<SymbolGapMap> {

	public QuotationsHandler(ListEvent<SymbolGapMap> notifier) {
		super(notifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {
		
		elementStack.push(qName);
		
		switch (qName) {
		case "quotations" :
			tempList = new ArrayList<SymbolGapMap>();
			break;
		case "quotation":
			SymbolGapMap gapMap = new SymbolGapMap(new Date());
			objectStack.add(gapMap);
			break;
		default:
			break;
		}
		
	}

	@Override
	void endElement(String qName) throws SAXException {
		
		elementStack.pop();
		
		switch (qName) {
		case "quotation":
			tempList.add(objectStack.pop());
			break;
		case "quotations":
			notifyCompleteProcess();
			break;
		default:
			break;
		}
	}

	@Override
	void characters(String value) throws SAXException {
		switch (currentElement()) {
		
		// do nothing
		case "quotations":
		case "quotation":
			break;
			
		case "board":
			objectStack.peek().setBoard(BoardType.valueOf(value));
			break;
			
		case "seccode":
			objectStack.peek().setSeccode(value);
			break;

		default:
			objectStack.peek().put(currentElement(), value);
			break;
		}
		

	}

}
