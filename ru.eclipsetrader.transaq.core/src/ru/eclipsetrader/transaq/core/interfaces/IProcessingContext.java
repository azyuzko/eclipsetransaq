package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDateTimeSupplier;

public interface IProcessingContext extends IDateTimeSupplier {
	
	void onTick(TQSymbol symbol, Tick tick);
	void onQuotesChange(TQSymbol symbol, QuoteGlass quoteGlass);
	void onCandleClose(TQSymbol symbol, CandleList candleList, Candle closedCandle);
	void onQuotationsChange(TQSymbol symbol, Quotation quotation);
	
	CandleType[] getCandleTypes();
	TQSymbol[] getSymbols();
	Instrument getInstrument(TQSymbol symbol);
	IAccount getAccount();
	
	void closePositions();
}
