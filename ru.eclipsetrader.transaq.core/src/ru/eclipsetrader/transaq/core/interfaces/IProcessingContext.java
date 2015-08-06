package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDateTimeSupplier;

public interface IProcessingContext extends IDateTimeSupplier {
	
	void onTick(Instrument instrument, Tick tick);
	void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass);
	void onCandleClose(Instrument instrument, CandleList candleList, Candle closedCandle);
	void onCandleOpen(Instrument instrument, CandleList candleList, Candle openedCandle);
	void onCandleChange(Instrument instrument, CandleList candleList, Candle changedCandle);
	void onQuotationsChange(Instrument instrument, Quotation quotation);
	
	CandleType[] getCandleTypes();
	TQSymbol[] getSymbols();
	Instrument getInstrument(TQSymbol symbol);
	IAccount getAccount();
	
	void closePositions();
}
