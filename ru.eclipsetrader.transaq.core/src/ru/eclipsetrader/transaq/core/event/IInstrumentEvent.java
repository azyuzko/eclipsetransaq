package ru.eclipsetrader.transaq.core.event;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IInstrumentEvent<T> {

	List<TQSymbol> getSymbolList();
	void addObserver(TQSymbol symbol, Observer<T> paramObserver);
	void notifyObservers(TQSymbol symbol, T paramObject);
	void deleteObserver(TQSymbol symbol, Observer<T> paramObserver);
}
