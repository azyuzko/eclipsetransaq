package ru.eclipsetrader.transaq.core.instruments;

import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.account.TQAccountService;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.event.InstrumentEvent;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
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
	
	public InstrumentEvent<List<Tick>> getDefaultTickListEvent() {
		return tickListEvent;
	}
	public InstrumentEvent<List<Quote>> getDefaultQuoteListEvent() {
		return quoteListEvent;
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
			public void OnStart() {
				// На старте ищем все инструменты из событий и запускаем на них подписку
				
				for (TQSymbol symbol : tickListEvent.getSymbolList()) {
					TQTickTradeService.getInstance().subscribeAllTrades(symbol);
				}

				for (TQSymbol symbol : quoteListEvent.getSymbolList()) {
					TQQuoteService.getInstance().subscribe(symbol);
				}

			}

			@Override
			public IAccount getAccount(TQSymbol symbol) {
				return TQAccountService.getInstance().getAccount(symbol);
			}
		};
		
	}
	
}
