package ru.eclipsetrader.transaq.core.event;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public abstract interface InstrumentObserver<T> {

	public abstract void update(TQSymbol symbol, T paramObject);
	
}