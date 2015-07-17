package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.internal.Market;

public class MarketHandler extends BaseXMLProcessor<Market> {

	public MarketHandler(String serverId, ListEvent<Market> bundleNotifier) {
		super(bundleNotifier);
		this.serverId = serverId;
	}
	
	public void startElement(String qName, Attributes attributes) throws SAXException {

		elementStack.push(qName);
        
        switch (QNAME.valueOf(qName)) {
			case markets:
				tempList = new ArrayList<Market>();
				break;
				
			case market :
				Market market = new Market(serverId);
				market.setId(Integer.valueOf(attributes.getValue("id")));
				objectStack.push(market);
				break;
				
			default:
				break;
		}
	}
	
    public void endElement(String qName) throws SAXException {

    	elementStack.pop();
    	
    	switch (QNAME.valueOf(qName)) {
			case markets:
				notifyCompleteProcess();					
				break;
				
			case market :
				Market market = objectStack.pop();
				tempList.add(market);
				break;
		default:
			break;

		}
    }
    
    public void characters(String value)
			throws SAXException {

    	switch (QNAME.valueOf(currentElement())) {
		case market:
	    	Market market = objectStack.peek();
			market.setName(value);
			break;
		default:
			break;

		}
    }
}
