package ru.eclipsetrader.transaq.core.trades;

import java.util.List;

import ru.eclipsetrader.transaq.core.event.IInstrumentEvent;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;

public interface IQuotationFeeder {

	IInstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder();
}
