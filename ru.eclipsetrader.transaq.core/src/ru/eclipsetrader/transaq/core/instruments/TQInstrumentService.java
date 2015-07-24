package ru.eclipsetrader.transaq.core.instruments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.event.InstrumentMassObserver;
import ru.eclipsetrader.transaq.core.event.InstrumentObserver;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.services.ITQInstrumentService;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQInstrumentService implements ITQInstrumentService {
	
	static TQInstrumentService instance;
	public static TQInstrumentService getInstance() {
		if (instance == null) {
			instance = new TQInstrumentService();
		}
		return instance;
	}
	
	Map<TQSymbol, Instrument> instruments = new HashMap<>();

	public Instrument getInstrument(TQSymbol symbol) {
		return instruments.get(symbol);
	}

	/**
	 * Наблюдатель за котировками бумаги
	 */
	InstrumentObserver<List<Quote>> iQuotesObserver = new InstrumentObserver<List<Quote>>() {
		@Override
		public void update(TQSymbol symbol, List<Quote> quotes) {
			if (instruments.containsKey(symbol)) {
				// обновим стакан
				instruments.get(symbol).updateQuotes(quotes);
			} else {
				// System.out.println("iQuotesObserver: Instrument not found");
			}
		}
	};
	
	public InstrumentObserver<List<Quote>> getIQuotesObserver() {
		return iQuotesObserver;
	}
	
	
	InstrumentMassObserver<TickTrade> iTickObserver = new InstrumentMassObserver<TickTrade>() {
		@Override
		public void update(TQSymbol symbol, List<TickTrade> list) {
			System.out.println("iTickObserver " + list.get(0).getSeccode() + " size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );

			if (instruments.containsKey(symbol)) {
				for (Tick tick : list) {
					instruments.get(symbol).processTrade(tick);
				}
			} else {
				// System.out.println("iTickObserver: Instrument not found");
			}

		}
	};
	
	public InstrumentMassObserver<TickTrade> getITickObserver() {
		return iTickObserver;
	}
	
}
