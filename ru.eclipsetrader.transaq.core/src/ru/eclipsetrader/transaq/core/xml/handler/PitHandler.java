package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.Pit;

public class PitHandler extends BaseXMLProcessor<Pit> {
	
    public PitHandler(ListEvent<Pit> bundleNotifier) {
		super(bundleNotifier);
	}


	public void startElement(String qName, Attributes attributes) throws SAXException {
	        
	        elementStack.push(qName);
	        
	        switch (QNAME.valueOf(qName)) {
				case pits:
					tempList = new ArrayList<Pit>();
					break;
				
				case pit:
					Pit pit = new Pit();
					pit.setSecCode(attributes.getValue("seccode"));
					pit.setBoard(BoardType.valueOf(attributes.getValue("board")));
					objectStack.push(pit);

					break;
					
				default:
					break;
			}					
    
    }

    
    public void endElement(String qName) throws SAXException {

	    	elementStack.pop();
	    	
	        switch (QNAME.valueOf(qName)) {
				case pits:
					notifyCompleteProcess();
					break;
				
				case pit:
					Pit pit = objectStack.pop();
					tempList.add(pit);
					break;
					
				default:
					break;
			}					
    }
    
    
    public void characters(String value)
			throws SAXException {

    	switch (QNAME.valueOf(currentElement())) {
    		case decimals:
    			objectStack.peek().setDecimals(Integer.valueOf(value));
    			break;
    		case market:
    			objectStack.peek().setMarket(Integer.valueOf(value));
    			break;
    		case minstep:
    			objectStack.peek().setMinStep(Double.valueOf(value));
    			break;
    		case lotsize:
    			objectStack.peek().setLotSize(Integer.valueOf(value));
    			break;
    		case point_cost:
    			objectStack.peek().setPoint_cost(Double.valueOf(value));
    			break;
		default:
			break;
    	}
    }
}
