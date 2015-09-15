package ru.eclipsetrader.transaq.core.helper;

import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.ICandleProcessContext;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.feed.TQDataFeed;
import ru.eclipsetrader.transaq.core.indicators.MA;
import ru.eclipsetrader.transaq.core.interfaces.IQuotesProcessingContext;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.util.LimitedArrayList;

import com.tictactec.ta.lib.MAType;


public class TransaqHelper  {

	Logger logger = LogManager.getLogger("TransaqHelper");
	
	TQSymbol symbol = TQSymbol.EDZ5;
	
	QuoteGlass quoteGlass = new QuoteGlass(symbol);
	CandleList cl_1M = new CandleList(symbol, CandleType.CANDLE_1M);
	CandleList cl_10S = new CandleList(symbol, CandleType.CANDLE_10S);
	
	TickList ticks = new TickList();
	MA maT = new MA(10, MAType.Trima);
	LimitedArrayList<QuoteGlass> quoteGlassList = new LimitedArrayList<QuoteGlass>(5);
	
	public TransaqHelper() {
		quoteGlass.setQuotesProcessingContext(quotesProcessingContext);
		cl_1M.setCandleProcessContext(cp_1M);
		cl_10S.setCandleProcessContext(cp_1M);
	}
	
	IQuotesProcessingContext quotesProcessingContext = (QuoteGlass quoteGlass) -> {
		//System.out.println(quoteGlass.toString(2));
		quoteGlassList.add(quoteGlass);
	};
	
	DirectedPosition dp = null;
	
	ICandleProcessContext cp_1M = (CandleList candleList, Candle closedCandle) -> {
		double price = closedCandle.getPriceValueByType(PriceType.VOLUME_WEIGHTED);
		logger.info("Candle " + candleList.getCandleType() +" closed: " + closedCandle.toString() + " " + price);
	};
	
	ICandleProcessContext cp_10S = (CandleList candleList, Candle closedCandle) -> {
		double price = closedCandle.getPriceValueByType(PriceType.WEIGHTED_CLOSE);
		logger.info("Candle " + candleList.getCandleType() +" closed: " + closedCandle.toString() + " " + price);
	};

	
	Observer<Order> newOrderObserver = (Order order) -> {
		//System.err.println("New order received  = " + order.getOrderDesc());
	};
	
	Observer<Trade> newTradeObserver = (Trade trade) -> {
		//System.err.println(trade);
	};
	
	Observer<List<Tick>> tickObserver = (List<Tick> tickList) -> {
		ticks.add(tickList);
	};
	
	public void start() {
		TQOrderTradeService.getInstance().newTradeEvent.addObserver(symbol, newTradeObserver);
		TQOrderTradeService.getInstance().newOrderEvent.addObserver(symbol, newOrderObserver);
		TQDataFeed.getInstance().subscribeTicksFeed(symbol, tickObserver);
		TQDataFeed.getInstance().subscribeQuotesFeed(symbol, quoteGlass);
		TQDataFeed.getInstance().subscribeCandlesFeed(symbol, cl_1M);
	}
	
	public static void main(String[] args) {
		CircularFifoQueue<Integer> fifo = new CircularFifoQueue<Integer>(2);
	    fifo.add(1);
	    fifo.add(2);
	    fifo.add(3);
	    System.out.println(fifo);
	}

}
