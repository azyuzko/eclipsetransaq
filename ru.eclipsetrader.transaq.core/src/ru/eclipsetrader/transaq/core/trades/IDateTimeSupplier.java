package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;

public interface IDateTimeSupplier {

	void setDateTime(Date date);
	Date getDateTime();

}
