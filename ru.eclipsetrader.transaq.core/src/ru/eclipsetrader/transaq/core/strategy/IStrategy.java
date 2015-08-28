package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.TQSymbol;

public interface IStrategy extends IProcessingContext {
	
	void start(IAccount account);
	void stop();
	
	IProcessingContext getProcessingContext();
	Instrument getInstrument(TQSymbol symbol);

}
