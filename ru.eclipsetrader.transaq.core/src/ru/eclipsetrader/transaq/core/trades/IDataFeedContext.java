package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.event.IInstrumentEvent;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;


public interface IDataFeedContext {

	void OnStart(Instrument[] instruments);
	
	IInstrumentEvent<List<Tick>> getTicksFeeder();
	IInstrumentEvent<List<Quote>> getQuotesFeeder();
	IInstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder();
	List<Candle> getCandleList(TQSymbol symbol, CandleType candleType, Date onDate, int count);

}
