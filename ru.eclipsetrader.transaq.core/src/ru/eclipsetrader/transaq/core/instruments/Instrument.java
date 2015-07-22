package ru.eclipsetrader.transaq.core.instruments;

import java.util.List;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleStorage;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.datastorage.TQCandleService;
import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.interfaces.ITQTickTrade;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;


public class Instrument {
	
	static ThreadGroup instrumentThreads = new ThreadGroup("Instrument threads groups");
	
	private TQSymbol symbol;
	private QuoteGlass glass = new QuoteGlass();
	private CandleStorage candleStorage;
	// private Map<IndicatorFunction, double[]> indicators = new HashMap<IndicatorFunction, double[]>();
	
	public Event<Object> onTick;

	public Instrument(TQSymbol symbol, CandleType[] candleTypes) {
		this.symbol = symbol;
		this.candleStorage = new CandleStorage(symbol);
		this.onTick = new Event<Object>("BaseInstrument.onTick", instrumentThreads);
		for (CandleType ct : candleTypes) {
			candleStorage.createCandleTypeList(ct);
		}
		// TQInstrumentService.getInstance().instruments.put(symbol, this);
	}
	
	public QuoteGlass getQuoteGlass() {
		return glass;
	}
	
	public CandleStorage getCandleStorage() {
		return candleStorage;
	}
	
	public void updateQuotes(List<Quote> quotes) {
		// обновим стакан
		for (Quote quote : quotes) {
			glass.update(quote);
		}
		// System.out.println("—такан обновлен");
	}
	
	// заполнить свечи историей
	public void backfillCandles(CandleType candleType, int count) {
		CandleList cl = candleStorage.getCandleList(candleType);
		cl.appendCandles(TQCandleService.getInstance().getHistoryData(symbol, candleType, count));
	}
	
	public void processTrade(ITQTickTrade tickTrade) {
		// добавим сделку в график свечей
		candleStorage.processTrade(tickTrade);
	}


}
