package ru.eclipsetrader.transaq.core.instruments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleStorage;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.datastorage.TQCandleService;
import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.indicators.Indicator;
import ru.eclipsetrader.transaq.core.interfaces.ITQTickTrade;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quote;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;


public class Instrument {
	
	public static int INIT_CANDLES = 10;
	
	static ThreadGroup instrumentThreads = new ThreadGroup("Instrument threads groups");
	
	private TQSymbol symbol;
	private QuoteGlass glass = new QuoteGlass();
	private CandleStorage candleStorage = new CandleStorage();
	private Map<String, Indicator> indicators = new HashMap<String, Indicator>();
	
	public Event<Object> onTick;

	Instrument(TQSymbol symbol) {
		this.symbol = symbol;
		this.onTick = new Event<Object>("BaseInstrument.onTick", instrumentThreads);
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
		// System.out.println("Стакан обновлен");
		// оповестим обработчики стратегий
		// не здесь! onTick.notifyObservers(quotes);
	}
	


	// получить свечки
	protected List<Candle> getHistoryCandles(CandleType candleType, int count) {
		return TQCandleService.getInstance().getHistoryData(symbol, candleType, count);
	}
	
	public void addCandleType(CandleType candleType) {
		for (CandleType ct :candleStorage.getCandleTypes()) {
			if (ct == candleType) {
				throw new RuntimeException("Candle type " + candleType + "already exists");
			}
		}
		CandleList cl = new CandleList(candleType);
		cl.appendCandles(getHistoryCandles(candleType, INIT_CANDLES));
		candleStorage.addCandleList(cl);
	}
	
	/*public void addIndicator(Indicator indicator, CandleType candleType) {
		String code = indicator.getCode();
		if (!indicators.containsKey(code)) {
			System.out.println("Adding indicator " + code);
			indicators.put(code, indicator);
			
			List<Candle> candles = getHistoryCandles(candleType, indicator.getLookback());
			
			System.out.println("candles size = " + candles.size());
			appendCandles(candleType, candles);
			initIndicators(candleType);
		} else {
			System.err.println("Indicator " + code + " already exist");
		}*
	}*/
	

	
	/*
	// инициализируем индикаторы
	protected void initIndicators(CandleType candleType) {
		for (Indicator indicator : indicators.values()) {
			if (!indicator.wasInitialized() && indicator.getCandleType() == candleType) {
				CandleList candleList = candleStorage.getCandles(indicator.getCandleType());
				if (candleList != null) {
					indicator.init(candleList);
				}
			}
		}
	}
	
	// пересчитаем индикаторы
	protected void processIndicators() {
		for (Indicator indicator : indicators.values()) {
			CandleList candleList = candleStorage.getCandles(indicator.getCandleType());
			if (candleList != null && indicator.wasInitialized()) {
				indicator.tick(candleList);
			}
		}
	}
	*/
	
	protected void processTrade(ITQTickTrade tickTrade) {
		// добавим сделку в график свечей
		candleStorage.processTrade(tickTrade);
		
		// пересчитаем индикаторы
		// processIndicators();
		
		// оповестим обработчики стратегий
		// Не здесь! onTick.notifyObservers(tickTrade);
	}

	/*protected void appendCandles(CandleType candleType, List<Candle> candles) {
		candleStorage.appendCandles(candleType, candles);
	}*/

}
