package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.internal.CandleKind;

public class CandleKindHandler extends BaseXMLProcessor<CandleKind> {


    public CandleKindHandler(String serverId, ListEvent<CandleKind> bundleNotifier) {
		super(bundleNotifier);
		this.serverId = serverId;
	}

	public void startElement(String qName, Attributes attributes) throws SAXException {
				
		elementStack.push(qName);
        
        switch (QNAME.valueOf(qName)) {
			case candlekinds:
				tempList = new ArrayList<CandleKind>();
				break;

			case kind :
				CandleKind candleKind = new CandleKind(serverId);
				objectStack.push(candleKind);
				break;
				
			default:
				break;
		}
	}
	
    public void endElement(String qName) throws SAXException {
    	
    	elementStack.pop();
    	
    	switch (QNAME.valueOf(qName)) {
			case candlekinds:
				notifyCompleteProcess();					
				break;
				
			case kind :
				CandleKind candleKind = objectStack.pop();
				tempList.add(candleKind);
				break;
				
			default:
				break;
		}
    }
    
    public void characters(String value)
			throws SAXException {

    	switch (QNAME.valueOf(currentElement())) {
		case id:
			objectStack.peek().setId(Integer.valueOf(value));
			break;

		case period:
			objectStack.peek().setPeriod(Integer.valueOf(value));
			break;
			
		case name:
			objectStack.peek().setName(value);
			break;
			
		default:
			break;
		}
    }
}
