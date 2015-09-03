package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.candle.ICandleProcessContext;
import ru.eclipsetrader.transaq.core.trades.IDateTimeSupplier;

public interface IProcessingContext extends ICandleProcessContext, IQuotesProcessingContext, IQuotationProcessingContext, IDateTimeSupplier {
	
}
