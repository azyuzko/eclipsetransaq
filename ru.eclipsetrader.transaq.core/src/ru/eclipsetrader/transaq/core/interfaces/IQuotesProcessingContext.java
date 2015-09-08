package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.QuoteGlass;

@FunctionalInterface
public interface IQuotesProcessingContext {
	void onQuotesChange(QuoteGlass quoteGlass);
}
