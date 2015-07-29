package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;

public interface IStrategy {
	
	void start();
	void stop();
	
	IDataFeedContext getDataFeedContext();
	IProcessingContext getProcessingContext();

}
