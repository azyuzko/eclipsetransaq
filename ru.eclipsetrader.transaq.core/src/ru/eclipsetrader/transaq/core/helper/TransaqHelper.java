package ru.eclipsetrader.transaq.core.helper;

import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.ICandleProcessContext;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.instruments.TQDataFeed;
import ru.eclipsetrader.transaq.core.interfaces.IQuotesProcessingContext;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.util.LimitedArrayList;


public class TransaqHelper  {

	TQSymbol symbol = TQSymbol.BRU5;
	
	QuoteGlass quoteGlass = new QuoteGlass(symbol);
	CandleList candleList = new CandleList(symbol, CandleType.CANDLE_10S);
	
	TickSet ticks = new TickSet(100);
	LimitedArrayList<QuoteGlass> quoteGlassList = new LimitedArrayList<QuoteGlass>(5);
	
	public TransaqHelper() {
		quoteGlass.setQuotesProcessingContext(quotesProcessingContext);
		candleList.setCandleProcessContext(candleProcessContext);
	}
	
	IQuotesProcessingContext quotesProcessingContext = new IQuotesProcessingContext() {
		@Override
		public void onQuotesChange(QuoteGlass quoteGlass) {
			updateQuotes(quoteGlass);
		}
	};
	
	ICandleProcessContext candleProcessContext = new ICandleProcessContext() {
		@Override
		public void onCandleClose(TQSymbol symbol, CandleList candleList,
				Candle closedCandle) {
			System.out.println("Candle closed! " + closedCandle.toString());
		}
	};
	
	Observer<Order> newOrderObserver = new Observer<Order>() {
		@Override
		public void update(Order order) {
			System.err.println("New order received  = " + order.getOrderDesc());
		}
	};
	
	Observer<List<Tick>> tickObserver = new Observer<List<Tick>>() {
		@Override
		public void update(List<Tick> tickList) {
			ticks.add(tickList);
		}
	};

	
	public void updateQuotes(QuoteGlass quoteGlass) {
		System.out.println("Quotes updated");
		quoteGlassList.add(quoteGlass);
		if (quoteGlassList.size() > 2) {
			quoteGlassList.last();
		}
	}
	
	public void start() {
		TQDataFeed.getInstance().subscribeTicksFeed(symbol, tickObserver);
		TQDataFeed.getInstance().subscribeQuotesFeed(symbol, quoteGlass);
		TQDataFeed.getInstance().subscribeCandlesFeed(symbol, candleList);
	}
	
	public static void main(String[] args) {
		CircularFifoQueue<Integer> fifo = new CircularFifoQueue<Integer>(2);
	    fifo.add(1);
	    fifo.add(2);
	    fifo.add(3);
	    System.out.println(fifo);
	}

}
