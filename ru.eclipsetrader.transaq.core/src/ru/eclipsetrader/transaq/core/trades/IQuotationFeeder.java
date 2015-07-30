package ru.eclipsetrader.transaq.core.trades;

import java.util.List;

import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;

public interface IQuotationFeeder {

	InstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder();
}
