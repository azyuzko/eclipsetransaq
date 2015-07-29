package ru.eclipsetrader.transaq.core.trades;

import java.util.List;

import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.model.internal.Tick;

public interface ITickFeeder {
	
	InstrumentEvent<List<Tick>> getTicksFeeder();
	
}
