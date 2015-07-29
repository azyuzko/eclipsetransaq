package ru.eclipsetrader.transaq.core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public class InstrumentEvent<T> {

	Map<TQSymbol, Event<T>> eventMap  = new HashMap<TQSymbol, Event<T>>();

	String name;
	
	public InstrumentEvent(String name) {
		this.name = name;
	}
	
	public List<TQSymbol> getSymbolList() {
		return new ArrayList<>(eventMap.keySet());
	}
	
	public synchronized void addObserver(TQSymbol symbol, Observer<T> paramObserver) {
		if (symbol == null || paramObserver == null) {
			throw new IllegalArgumentException();
		}
		
		Event<T> event = null;
		
		if (eventMap.containsKey(symbol)) {
			event = eventMap.get(symbol);
		} else {
			event = new Event<T>(name);
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
	
	public void deleteObserver(TQSymbol symbol, Observer<T> paramObserver) {
		if (eventMap.containsKey(symbol)) {
			Event<T> event = eventMap.get(symbol);
			event.deleteObserver(paramObserver);
		}
	}
	
}
