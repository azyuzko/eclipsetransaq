package ru.eclipsetrader.transaq.core.trades;


public interface IDataFeedContext extends ITickFeeder, IQuoteFeeder, IHistoryFeeder, IAccountSupplier {

	void OnStart();
	
}
