package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.ClientType;
import ru.eclipsetrader.transaq.core.model.internal.Client;

public class ClientHandler extends BaseXMLProcessor<Client> {


    public ClientHandler(String serverId, Event<Client> bundleNotifier) {
		super(bundleNotifier);
		this.serverId = serverId;
	}

    public void startElement(String qName, Attributes attributes) throws SAXException {

	        elementStack.push(qName);
	        
	        switch (QNAME.valueOf(qName)) {

				case client:
					Client client = new Client(serverId);
					client.setId(attributes.getValue("id"));
					client.setRemove(getBoolean(attributes.getValue("remove")));
					objectStack.push(client);
					break;
					
				default:
					break;
			}					
    
    }

    
    public void endElement(String qName) throws SAXException {

	    	elementStack.pop();
	    	
	        switch (QNAME.valueOf(qName)) {
		
				case client:
					notifyCompleteElement(objectStack.pop());
					break;
					
				default:
					break;
			}
    }
    
    
    public void characters(String value)
			throws SAXException {

    	// System.out.println("characters: " + value);

    	switch (QNAME.valueOf(currentElement())) {
    		case currency :
    			objectStack.peek().setCurrency(value);
    			break;
    			
    		case type :
    			objectStack.peek().setType(ClientType.valueOf(value.toUpperCase()));
    			break;
    			
    		case ml_intraday :
    			objectStack.peek().setMl_intraday(Double.valueOf(value));
    			break;
    			
    		case ml_overnight :
    			objectStack.peek().setMl_overnight(Double.valueOf(value));
    			break;

    		case ml_restrict :
    			objectStack.peek().setMl_restrict(Double.valueOf(value));
    			break;

    		case ml_call :
    			objectStack.peek().setMl_call(Double.valueOf(value));
    			break;

    		case ml_close :
    			objectStack.peek().setMl_close(Double.valueOf(value));
    			break;
    			
	    	default :
	    		throw new RuntimeException("Встретился неизвестный элемент " + currentElement());
    	}
    }
}
