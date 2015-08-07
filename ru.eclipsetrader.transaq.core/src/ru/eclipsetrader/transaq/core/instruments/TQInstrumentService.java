package ru.eclipsetrader.transaq.core.instruments;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.account.TQAccountService;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.services.ITQInstrumentService;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;

public class TQInstrumentService implements ITQInstrumentService {
	
	static TQInstrumentService instance;
	public static TQInstrumentService getInstance() {
		if (instance == null) {
			instance = new TQInstrumentService();
		}
		return instance;
	}

	InstrumentEvent<List<Tick>> tickListEvent = new InstrumentEvent<>("TQInstrumentService tick event");
	InstrumentEvent<List<Quote>> quoteListEvent = new InstrumentEvent<>("TQInstrumentService quote event");
	InstrumentEvent<List<SymbolGapMap>> quotationGapListEvent = new InstrumentEvent<>("TQInstrumentService quotation gap event");
	
	public InstrumentEvent<List<Tick>> getDefaultTickListEvent() {
		return tickListEvent;
	}
	public InstrumentEvent<List<Quote>> getDefaultQuoteListEvent() {
		return quoteListEvent;
	}
	public InstrumentEvent<List<SymbolGapMap>> getQuotationGapListEvent() {
		return quotationGapListEvent;
	}
	
	public IDataFeedContext getDefaultDataFeedContext() {
		
		return new IDataFeedContext() {
			
			@Override
			public List<Candle> getCandleList(TQSymbol symbol, CandleType candleType,
					Date fromDate, int count) {
				return TQCandleService.getInstance().getHistoryData(symbol, candleType, count);
			}
			
			@Override
			public InstrumentEvent<List<Quote>> getQuotesFeeder() {
				return quoteListEvent;
			}
			
			@Override
			public InstrumentEvent<List<Tick>> getTicksFeeder() {
				return tickListEvent;
			}
			
			@Override
			public InstrumentEvent<List<SymbolGapMap>> getQuotationGapsFeeder() {
				return quotationGapListEvent;
			}

			@Override
			public void OnStart(Instrument[] instruments) {
				List<TQSymbol> list = new ArrayList<TQSymbol>();
				for (Instrument i : instruments) {
					list.add(i.getSymbol());
					i.init();
				}
				
				// На старте запускаем подписку
				TQTickTradeService.getInstance().subscribeAllTrades(list);
				TQQuoteService.getInstance().subscribe(list);
				TQQuotationService.getInstance().subscribe(list);
			}

		};
		
	}
	
}
