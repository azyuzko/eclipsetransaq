package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;

public interface IStrategy extends IProcessingContext {
	
	void start(IAccount account);
	void stop();
	
	IDataFeedContext getDataFeedContext();
	IProcessingContext getProcessingContext();

}
