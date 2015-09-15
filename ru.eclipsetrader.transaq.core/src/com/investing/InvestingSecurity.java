package com.investing;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.TQAccountService;
import ru.eclipsetrader.transaq.core.feed.TQDataFeed;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IQuotesProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.orders.OrderCallback;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.quotes.Spread;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.LimitedArrayList;

public class InvestingSecurity {
	
	Logger logger = LogManager.getLogger("InvestingSecurity");

	TQSymbol symbol;
	InvestingSymbol investingSymbol; 	// код бумаги на investing.com
	int operQuantity; // кол-во бумаг для покупки-продажи в 1 лоте
	int requestPeriod; // in seconds
	List<InvestingRequest> requests = new LimitedArrayList<>(100);

	QuoteGlass quoteGlass;

	public InvestingSecurity(TQSymbol symbol, InvestingSymbol investingSymbol, int operQuantity, int period) {
		this.symbol = symbol;
		this.investingSymbol = investingSymbol;
		this.operQuantity = operQuantity;
		this.requestPeriod = period;
		this.quoteGlass = new QuoteGlass(symbol);
	}
	
	IQuotesProcessingContext quotesProcessingContext = (quoteGlass) -> {
		
	};
	
	OrderCallback callback = new OrderCallback() {
		@Override
		public void onExecuted(Order order) {
			System.err.println("Executed " + order.getOrderDesc());
		}
	};
	
	public void processRequest(InvestingRequest investingRequest) {
		InvestingRequest current = investingRequest;
		logger.debug(symbol + " " + current.toString());
		if (requests.size() > 0) {
			InvestingRequest last = requests.get(requests.size()-1);
			if (last.signal != current.signal) {
				changeState(last.signal, current.signal);
			}
		} else {
			// init
			changeState(null, current.signal);
		}
		requests.add(current);
	}
	
	public int multiply(int count) {
		return count * operQuantity;
	}
	
	public void changeState(InvestingState fromState, InvestingState toState) {
		System.err.println(symbol + " Changing state from <" + fromState + ">  to  <" + toState + ">");
		int count = 0;
		if (fromState == null) {
			logger.debug("Init monitor positions..");
			int position = getAccount().getPosition(symbol);
			logger.debug("Current position: " + position);
			count = multiply(toState.value) - position;
		} else {
			count = multiply(toState.getCountFrom(fromState));
		}
		logger.debug("Count: " + count);
		if (count == 0) {
			return;
		}
		double price;
		double step = TQSecurityService.getInstance().get(symbol) != null ? TQSecurityService.getInstance().get(symbol).getMinStep() : 0;
		Spread quoteSpread = quoteGlass.getSpread();
		BuySell buySell = count > 0 ? BuySell.B : BuySell.S;
		if (buySell == BuySell.B) {
			price = quoteSpread.getBuy() + step;
		} else {
			price = quoteSpread.getSell() - step;
		}
		int quantity = Math.abs(count);
		OrderRequest orderRequest = OrderRequest.createRequest(symbol, buySell, price, quantity);
		orderRequest.setBrokerref("ST_IS_" + requestPeriod);
		logger.debug("Created order request " + orderRequest);
		TQOrderTradeService.getInstance().createOrder(orderRequest, callback);
	}
	
	IAccount getAccount() {
		return TQAccountService.getInstance().getAccount(symbol);
	}
	
	public void onStart() {
		if (TransaqServer.getInstance() != null) {
			TQDataFeed.getInstance().subscribeQuotesFeed(symbol, quoteGlass);
			quoteGlass.setQuotesProcessingContext(quotesProcessingContext);
		}
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public int getOperQuantity() {
		return operQuantity;
	}

	public int getRequestPeriod() {
		return requestPeriod;
	}

	public InvestingSymbol getInvestingSymbol() {
		return investingSymbol;
	}
	

	/*
	 * static { list.add(new InvestingSecurity(TQSymbol.EDZ5, "1", 1));
	 * list.add(new InvestingSecurity(TQSymbol.BRV5, "8833", 5)); list.add(new
	 * InvestingSecurity(TQSymbol.RIZ5, "13665", 1)); list.add(new
	 * InvestingSecurity(TQSymbol.MXZ5, "13666", 1)); }
	 */

}
