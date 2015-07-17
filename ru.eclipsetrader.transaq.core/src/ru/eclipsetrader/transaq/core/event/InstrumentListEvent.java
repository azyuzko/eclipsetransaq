package ru.eclipsetrader.transaq.core.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.model.TQSymbol;


public class InstrumentListEvent<T> extends ListEvent<T> {
	
	public InstrumentListEvent(String name, ThreadGroup threadGroup) {
		super(name, threadGroup);
	}

	Map<TQSymbol, ListEvent<T>> eventMap  = new HashMap<TQSymbol, ListEvent<T>>();
	
	public synchronized void addObserver(TQSymbol symbol, MassObserver<T> paramObserver) {
		if (symbol == null || paramObserver == null) {
			throw new IllegalArgumentException();
		}
		ListEvent<T> event = null;
		if (eventMap.containsKey(symbol)) {
			event = eventMap.get(symbol);
		} else {
			event = new ListEvent<T>(name, thread.getThreadGroup());
			eventMap.put(symbol, event);
		}
		event.addObserver(paramObserver);
	}
	
	public void notifyObservers(List<T> paramObject) {
		throw new IllegalAccessError();
	}
	
	public void notifyObservers(TQSymbol symbol, List<T> paramObject) {
		if (eventMap.containsKey(symbol)) {
			ListEvent<T> event = eventMap.get(symbol);
			event.notifyObservers(paramObject);
		}
	}
	
	public void deleteObserver(TQSymbol symbol, MassObserver<T> paramObserver) {
		if (eventMap.containsKey(symbol)) {
			ListEvent<T> event = eventMap.get(symbol);
			event.deleteObserver(paramObserver);
		}
	}
}
