package ru.eclipsetrader.transaq.core.event;

import java.util.List;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

public abstract interface InstrumentMassObserver<T> {

	public abstract void update(TQSymbol symbol, List<T> list);
	
}