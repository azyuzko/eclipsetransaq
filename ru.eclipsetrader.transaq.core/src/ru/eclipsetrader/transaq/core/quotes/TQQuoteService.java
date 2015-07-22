package ru.eclipsetrader.transaq.core.quotes;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.services.ITQQuoteService;

public class TQQuoteService implements ITQQuoteService, Closeable {
	
	ArrayBlockingQueue<List<Quote>> quoteQueue = new ArrayBlockingQueue<List<Quote>>(300);
	
	Thread dbWriteThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<Quote> list = quoteQueue.take();
					DataManager.batchQuoteList(list);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
	
	static TQQuoteService instance;
	public static TQQuoteService getInstance() {
		if (instance == null) {
			instance = new TQQuoteService();
		}
		return instance;
	}
	
	private TQQuoteService() {
		dbWriteThread.start();
	}
	
	@Override
	public void close() {
		dbWriteThread.interrupt();
	}
	
	Observer<List<SymbolGapMap>> quoteGapObserver = new Observer<List<SymbolGapMap>>(){
		@Override
		public void update(List<SymbolGapMap> gapList) {
			Map<TQSymbol, List<Quote>> quoteMap = applyQuoteGap(gapList);
			for (TQSymbol symbol : quoteMap.keySet()) {
				List<Quote> quotesList = quoteMap.get(symbol);
				quoteQueue.add(quotesList);
				TQInstrumentService.getInstance().getIQuotesObserver().update(symbol, quotesList);
			}
		}
	};
	
	public Observer<List<SymbolGapMap>> getQuoteGapObserver() {
		return quoteGapObserver;
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
			Quote quote = new Quote(key.getBoard(), key.getSeccode());
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