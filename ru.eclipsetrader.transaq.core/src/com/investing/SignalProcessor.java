package com.investing;

import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.Settings;
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
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.MailUtils;

public class SignalProcessor {
	
	Logger logger = LogManager.getLogger("SignalProcessor");
	
	TQSymbol symbol;
	int operQuantity; // кол-во бумаг для покупки-продажи в 1 лоте
	
	double step = 0;
	QuoteGlass quoteGlass;
	
	LinkedList<InvestingSignal> signalQueue = new LinkedList<InvestingSignal>();
	
	LinkedList<Order> orderQueue = new LinkedList<Order>(); 
	
	IQuotesProcessingContext quotesProcessingContext = (quoteGlass) -> {
		if (orderQueue.size() > 0) {
			if (step == 0) {
				System.err.println("step not initialized");
				return;
			}
			double maxBuy = quoteGlass.getSpread().getBuy();
			double minSell = quoteGlass.getSpread().getSell();
			Order order = orderQueue.get(0);
			double newPrice = 0;
			if (order.getBuysell() == BuySell.B) {
				newPrice = maxBuy - step*5;
			} else {
				newPrice = minSell + step*5;
			}
			System.out.println("Move to " + newPrice);
			TQOrderTradeService.getInstance().moveOrder(order.getOrderno(), newPrice);
		}
	};
	
	OrderCallback callback = new OrderCallback() {
		
	};

	public SignalProcessor(TQSymbol symbol, int operQuantity){
		this.symbol = symbol;
		this.operQuantity = operQuantity;
		this.quoteGlass = new QuoteGlass(symbol);
	}
	
	public void init() {
		if (TransaqServer.getInstance() != null) {
			TQDataFeed.getInstance().subscribeQuotesFeed(symbol, quoteGlass);
			quoteGlass.setQuotesProcessingContext(quotesProcessingContext);
			step = TQSecurityService.getInstance().get(symbol).getMinStep();
			logger.info(symbol + " - Security step <" + step + ">") ;
		}
	}
	
	
	public QuoteGlass getQuoteGlass() {
		return quoteGlass;
	}
	
	
	IAccount getAccount() {
		return TQAccountService.getInstance().getAccount(symbol);
	}
	
	public void processSignal(InvestingSignal investingSignal) {
		System.err.println(symbol + " Received signal  <" + investingSignal.getToState() + ">");
		int position = getAccount().getPosition(symbol);
		int count = investingSignal.getToState().getValue() * operQuantity - position;
		logger.debug("Current position = " + position + ", count = " + count);
		process(count);
	}
	
	void process(int count) {
		logger.debug("Count: " + count);
		if (count == 0) {
			return;
		}
		BuySell buySell = count > 0 ? BuySell.B : BuySell.S;
		int quantity = Math.abs(count);
		OrderRequest orderRequest = OrderRequest.createByMarketRequest(symbol, buySell, quantity);
		//orderRequest.setBrokerref();
		logger.debug("Created order request " + orderRequest);
		Order order = TQOrderTradeService.getInstance().createOrder(orderRequest, callback);
		
		String message = "";
		if (order != null) {
			message = "Executed signal <b>" + buySell + "</b> on <b>" + symbol.getSeccode() + "</b>, quantity = " + quantity + "<br>"
					+ TQAccountService.getInstance().getAccount(symbol).toString().replace("\n", "<br>");
		} else {
			message = "Order execution failed with message <" + orderRequest.getErrorMessage() + ">";
		}
		
		MailUtils.sendHTMLMail(Settings.MAIL_NOTIFICATION_ADDRESS, "Signal execution notification", message);
	}
	

	public int getOperQuantity() {
		return operQuantity;
	}
}
