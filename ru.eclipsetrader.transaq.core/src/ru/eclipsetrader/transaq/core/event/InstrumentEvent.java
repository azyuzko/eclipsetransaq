package ru.eclipsetrader.transaq.core.event;

import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public class InstrumentEvent<T> {

	Map<TQSymbol, Event<T>> eventMap  = new HashMap<TQSymbol, Event<T>>();
	
	ThreadGroup threadGroup;
	String name;
	
	public InstrumentEvent(String name, ThreadGroup threadGroup) {
		this.name = name;
		this.threadGroup = threadGroup;
	}
	
	public synchronized void addObserver(TQSymbol symbol, Observer<T> paramObserver) {
		if (symbol == null || paramObserver == null) {
			throw new IllegalArgumentException();
		}
		
		Event<T> event = null;
		
		if (eventMap.containsKey(symbol)) {
			event = eventMap.get(symbol);
		} else {
			event = new Event<T>(name, threadGroup);
			eventMap.put(symbol, event);
		}
		event.addObserver(paramObserver);
	}
	
	public void notifyObservers(TQSymbol symbol, T paramObject) {
		if (symbol == null) {
			throw new IllegalArgumentException();
		}
		if (eventMap.containsKey(symbol)) {
			Event<T> event = eventMap.get(symbol);
			event.notifyObservers(paramObject);
		}
	}
	
	public void deleteObserver(String instrumentId, Observer<T> paramObserver) {
		if (eventMap.containsKey(instrumentId)) {
			Event<T> event = eventMap.get(instrumentId);
			event.deleteObserver(paramObserver);
		}
	}
	
}
