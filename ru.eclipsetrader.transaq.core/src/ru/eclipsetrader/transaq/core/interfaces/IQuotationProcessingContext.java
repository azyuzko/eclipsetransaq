package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;

@FunctionalInterface
public interface IQuotationProcessingContext {
	void onQuotationsChange(TQSymbol symbol, Quotation quotation);
}
