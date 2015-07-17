package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.event.ListEvent;

public abstract class BaseXMLProcessor<T> {

	private Event<List<T>> objectListNotifier;
	private Event<T> objectNotifier;	
	
	protected Stack<String> elementStack = new Stack<String>();
	protected Stack<T> objectStack  = new Stack<T>();
	
    protected List<T> tempList;
    
    protected String serverId;
	
    protected String currentElement() {
        return this.elementStack.peek();
    }
    
    protected void notifyCompleteProcess() {
    	if (objectListNotifier != null) {
    		objectListNotifier.notifyObservers(tempList);
    	}
    }
    
    protected void notifyCompleteElement(T element) {
    	if (objectNotifier != null) {
    		objectNotifier.notifyObservers(element);
    	}
    }

    public BaseXMLProcessor(Event<T> notifier, ListEvent<T> bundleNotifier) {
    	this.objectNotifier = notifier;
    	this.objectListNotifier = bundleNotifier;
    }
    
    public BaseXMLProcessor(Event<T> notifier) {
    	this.objectNotifier = notifier;
    }

    public BaseXMLProcessor(ListEvent<T> bundleNotifier) {
    	this.objectListNotifier = bundleNotifier;
    }

	protected static boolean getBoolean(String value) {
    	return ("true".equals(value) || "yes".equals(value)) ? true : false; 
    }    
	
	abstract void startElement(String qName, Attributes attributes) throws SAXException;
	abstract void endElement(String qName) throws SAXException;
	abstract void characters(String value) throws SAXException;
	
}
