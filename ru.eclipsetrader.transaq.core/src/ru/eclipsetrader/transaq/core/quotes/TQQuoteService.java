package ru.eclipsetrader.transaq.core.quotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.data.DatabaseManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.services.ITQQuoteService;

public class TQQuoteService implements ITQQuoteService {
	
	Logger logger = LogManager.getLogger(TQQuoteService.class);
	
	static TQQuoteService instance;
	public static TQQuoteService getInstance() {
		if (instance == null) {
			instance = new TQQuoteService();
		}
		return instance;
	}
	
	Observer<List<SymbolGapMap>> quoteGapObserver = new Observer<List<SymbolGapMap>>(){
		@Override
		public void update(List<SymbolGapMap> gapList) {
			Map<TQSymbol, List<Quote>> quoteMap = applyQuoteGap(gapList);
			for (TQSymbol symbol : quoteMap.keySet()) {
				List<Quote> quotesList = quoteMap.get(symbol);
				TQInstrumentService.getInstance().getDefaultQuoteListEvent().notifyObservers(symbol, quotesList);
				DatabaseManager.writeQuotes(quotesList);
			}
		}
	};
	
	public Observer<List<SymbolGapMap>> getQuoteGapObserver() {
		return quoteGapObserver;
	}
	
	public static Map<TQSymbol, List<Quote>> createMap(List<Quote> quoteList) {
		Map<TQSymbol, List<Quote>> result = new HashMap<TQSymbol, List<Quote>>();
		for (Quote quote : quoteList) {
			TQSymbol symbol = new TQSymbol(quote.getBoard(), quote.getSeccode());
			List<Quote> list = result.get(symbol);
			if (list == null) {
				list = new ArrayList<Quote>();
				result.put(symbol, list);
			}
			list.add(quote);
		}
		return result;
	}
	
	public static Map<TQSymbol, List<Quote>> applyQuoteGap(List<SymbolGapMap> quoteGapList) {
		
		Map<TQSymbol, List<Quote>> quoteMap = new HashMap<TQSymbol, List<Quote>>();
		
		for (SymbolGapMap quoteGap : quoteGapList) {
			TQSymbol key = new TQSymbol(quoteGap.getBoard(), quoteGap.getSeccode());
			List<Quote> quoteList = quoteMap.get(key);
			if (quoteList == null) {
				quoteList = new ArrayList<Quote>();
				quoteMap.put(key, quoteList);
			}
			Quote quote = new Quote(quoteGap.getTime(), key.getBoard(), key.getSeccode());
			for (String attr : quoteGap.keySet()) {
				if ("buy".equals(attr)) quote.setBuy(Integer.valueOf(quoteGap.get(attr)));				
				else if ("price".equals(attr)) quote.setPrice(Double.valueOf(quoteGap.get(attr)));
				else if ("sell".equals(attr)) quote.setSell(Integer.valueOf(quoteGap.get(attr)));
				//else throw new UnimplementedException("Field " + attr +" not implemented");
			}
			quoteList.add(quote);
		}
		return quoteMap;
	}

	@Override
	public void subscribe(TQSymbol symbol) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		subscribeCommand.subscribeQuotes(symbol);
		TransaqLibrary.SendCommand(subscribeCommand.createConnectCommand());
	}

	@Override
	public void unsubscribe(TQSymbol symbol) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		subscribeCommand.subscribeQuotes(symbol);
		TransaqLibrary.SendCommand(subscribeCommand.createUnsubscribeCommand());
	}

	public void subscribe(List<TQSymbol> symbols) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		for (TQSymbol symbol : symbols) {
			subscribeCommand.subscribeQuotes(symbol);
		}
		TransaqLibrary.SendCommand(subscribeCommand.createConnectCommand());
		
	}

	public void unsubscribe(List<TQSymbol> symbols) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		for (TQSymbol symbol : symbols) {
			subscribeCommand.subscribeQuotes(symbol);
		}
		TransaqLibrary.SendCommand(subscribeCommand.createUnsubscribeCommand());
	}



}
