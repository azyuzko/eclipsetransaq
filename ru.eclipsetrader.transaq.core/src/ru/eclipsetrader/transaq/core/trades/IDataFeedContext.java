package ru.eclipsetrader.transaq.core.trades;


public interface IDataFeedContext extends ITickFeeder, IQuoteFeeder, IQuotationFeeder, IHistoryFeeder, IAccountSupplier {

	void OnStart();
	
}
