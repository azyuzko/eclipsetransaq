package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.internal.Board;

public class BoardHandler extends BaseXMLProcessor<Board> {


	public BoardHandler(String serverId, ListEvent<Board> bundleNotifier) {
		super(bundleNotifier);
		this.serverId = serverId;
	}

	public void startElement(String qName, Attributes attributes)
			throws SAXException {

		elementStack.push(qName);

		switch (QNAME.valueOf(qName)) {
		case boards:
			tempList = new ArrayList<Board>();
			break;

		case board:
			Board board = new Board(serverId);
			board.setId(attributes.getValue("id"));
			objectStack.push(board);
			break;
		default:
			break;

		}
	}

	public void endElement(String qName) throws SAXException {

		elementStack.pop();

		switch (QNAME.valueOf(qName)) {

		case boards:
			notifyCompleteProcess();
			break;

		case board:
			Board board = objectStack.pop();
			tempList.add(board);
			break;
		default:
			break;

		}

	}

	public void characters(String value) throws SAXException {

		switch (QNAME.valueOf(currentElement())) {
		case name:
			objectStack.peek().setName(value);
			break;
		case market:
			objectStack.peek().setMarket(value);
			break;
		case type:
			objectStack.peek().setType(value);
			break;
		default:
			break;
		}

	}

}
