package ru.eclipsetrader.transaq.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * Синхронный обработчик событий
 * Используется для эмулятора, из-за переполнения очередей в InstrumentEvent
 * @author Zyuzko-AA
 *
 * @param <T>
 */
public class SynchronousInstrumentEvent<T> implements IInstrumentEvent<T> {

	Map<TQSymbol, Observer<T>> observers = Collections.synchronizedMap(new HashMap<TQSymbol, Observer<T>>());
	
	String name;
	
	public SynchronousInstrumentEvent(String name) {
		this.name = name;
	}
	
	@Override
	public List<TQSymbol> getSymbolList() {
		return new ArrayList<>(observers.keySet());
	}

	@Override
	public void addObserver(TQSymbol symbol, Observer<T> paramObserver) {
		observers.put(symbol, paramObserver);
	}

	@Override
	public void notifyObservers(TQSymbol symbol, T paramObject) {
		Observer<T> observer = observers.get(symbol);
		if (observer != null) {
			observer.update(paramObject);
		}
	}

	@Override
	public void deleteObserver(TQSymbol symbol, Observer<T> paramObserver) {
		observers.remove(symbol);
	}

}
