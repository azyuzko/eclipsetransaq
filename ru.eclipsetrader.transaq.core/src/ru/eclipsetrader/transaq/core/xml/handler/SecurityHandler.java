package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.datastorage.TQMarketService;
import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.SecurityType;
import ru.eclipsetrader.transaq.core.model.internal.Security;

public class SecurityHandler extends BaseXMLProcessor<Security> {

    public SecurityHandler(String serverId, ListEvent<Security> bundleNotifier) {
		super(bundleNotifier);
		this.serverId = serverId;
	}

    public void startElement(String qName, Attributes attributes) throws SAXException {

	        elementStack.push(qName);
	        
	        switch (QNAME.valueOf(qName)) {
	        
	        	case securities:
	        		tempList = new ArrayList<Security>();
	        		break;
				
				case security: {
					Security security = new Security(serverId);
					security.setId(Integer.valueOf(attributes.getValue("secid")));
					security.setActive(getBoolean(attributes.getValue("active")));
					objectStack.push(security);
				}
					break;
				
				case opmask: {
					Security security = objectStack.peek();
					security.setUseCredit(getBoolean(attributes.getValue("usecredit")));
					security.setByMarket(getBoolean(attributes.getValue("bymarket")));
					security.setNoSplit(getBoolean(attributes.getValue("nosplit")));
					security.setImmorCancel(getBoolean(attributes.getValue("immorcancel")));
					security.setCancelBalance(getBoolean(attributes.getValue("cancelbalance")));
				}
					break;
			default:
				break;
					
			}					
    
    }

    
    public void endElement(String qName) throws SAXException {

	    	elementStack.pop();
	    	
	        switch (QNAME.valueOf(qName)) {
				case securities:
					notifyCompleteProcess();
					break;
				
				case security:
					Security security = objectStack.pop();
					tempList.add(security);
					break;
			default:
				break;

			}					
    }
    
    
    public void characters(String value) throws SAXException {

    	switch (QNAME.valueOf(currentElement())) {
    		case sec_tz:
    			objectStack.peek().setTZ(value);
    			break;
    		case seccode:
    			objectStack.peek().setSeccode(value);
    			break;
    		case board:
    			objectStack.peek().setBoard(BoardType.valueOf(value));
    			break;
    		case shortname:
    			objectStack.peek().setShortName(value);
    			break;
    		case decimals:
    			objectStack.peek().setDecimals(Integer.valueOf(value));
    			break;
    		case market:
    			objectStack.peek().setMarket(TQMarketService.getInstance().getMarketType(Integer.valueOf(value)));
    			break;
    		case sectype:
    			objectStack.peek().setType(SecurityType.valueOf(value));    			
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
