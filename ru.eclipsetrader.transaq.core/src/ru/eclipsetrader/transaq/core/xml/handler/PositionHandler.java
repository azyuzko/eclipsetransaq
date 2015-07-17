package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.PositionType;
import ru.eclipsetrader.transaq.core.util.Holder;

public class PositionHandler extends BaseXMLProcessor<Holder<PositionType, Map<String, String>>> {

    
    public PositionHandler(Event<Holder<PositionType, Map<String, String>>> positionCallback) {
    	super(positionCallback);
    }
    

	public void startElement(String qName, Attributes attributes) throws SAXException {
	        elementStack.push(qName);
	        
	        switch (QNAME.valueOf(qName)) {

				case sec_position:
					Holder<PositionType, Map<String, String>> securityGap = new Holder<PositionType, Map<String,String>>(PositionType.SECURITY, 
							new HashMap<String, String>());
					objectStack.push(securityGap);
					break;
					
				case money_position:
					Holder<PositionType, Map<String, String>> moneyGap = new Holder<PositionType, Map<String,String>>(PositionType.MONEY, 
							new HashMap<String, String>());
					objectStack.push(moneyGap);
					break;

				case forts_position:
					Holder<PositionType, Map<String, String>> fortsGap = new Holder<PositionType, Map<String,String>>(PositionType.FORTS, 
							new HashMap<String, String>());
					objectStack.push(fortsGap);
					break;

				case forts_money:
					Holder<PositionType, Map<String, String>> fortsMoneyGap = new Holder<PositionType, Map<String,String>>(PositionType.FORTS_MONEY, 
							new HashMap<String, String>());
					objectStack.push(fortsMoneyGap);
					break;
					
					
				default:
					break;
			}
	    }

	    public void endElement(String qName) throws SAXException {

	    	elementStack.pop();
	    	
			switch (QNAME.valueOf(qName)) {

				case sec_position:
				case money_position:
				case forts_position:
				case forts_money:
					notifyCompleteElement(objectStack.pop());
					break;

				default:
					break;
			}
	    	
	    }
	    
	    public void characters(String value)
				throws SAXException {

	    	 Holder<PositionType, Map<String, String>> positionGap = objectStack.peek();
	    	 if (!"markets".equals(currentElement())) {
	    		 positionGap.getSecond().put(currentElement(), value);
	    	 }
	    	 
	    	}

}
