package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;


public interface IDataFeedContext extends ITickFeeder, IQuoteFeeder, IQuotationFeeder, IHistoryFeeder, IAccountSupplier {

	void OnStart();
	Date currentDate();
}
