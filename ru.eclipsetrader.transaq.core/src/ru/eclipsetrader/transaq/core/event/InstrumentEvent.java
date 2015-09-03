package ru.eclipsetrader.transaq.core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public class InstrumentEvent<T> implements IInstrumentEvent<T> {

	Map<TQSymbol, Event<T>> eventMap  = new HashMap<TQSymbol, Event<T>>();

	String name;
	
	public InstrumentEvent(String name) {
		this.name = name;
	}
	
	@Override
	public List<TQSymbol> getSymbolList() {
		return new ArrayList<>(eventMap.keySet());
	}
	
	@Override
	public synchronized void addObserver(TQSymbol symbol, Observer<T> paramObserver) {
		Objects.requireNonNull(symbol);
		Objects.requireNonNull(paramObserver);
		
		Event<T> event = eventMap.getOrDefault(symbol, new Event<T>(name));
		event.addObserver(paramObserver);
		eventMap.putIfAbsent(symbol, event);
	}
	
	@Override
	public void notifyObservers(TQSymbol symbol, T paramObject) {
		Objects.requireNonNull(symbol);
		if (eventMap.containsKey(symbol)) {
			Event<T> event = eventMap.get(symbol);
			event.notifyObservers(paramObject);
		}
	}
	
	@Override
	public void deleteObserver(TQSymbol symbol, Observer<T> paramObserver) {
		if (eventMap.containsKey(symbol)) {
			Event<T> event = eventMap.get(symbol);
			event.deleteObserver(paramObserver);
		}
	}
	
}
