package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;

public interface IQuotationProcessingContext {
	void onQuotationsChange(TQSymbol symbol, Quotation quotation);
}
