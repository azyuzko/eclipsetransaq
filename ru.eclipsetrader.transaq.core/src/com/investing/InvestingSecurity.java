package com.investing;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.LimitedArrayList;

public class InvestingSecurity {
	
	Logger logger = LogManager.getLogger("InvestingSecurity");

	TQSymbol symbol;
	InvestingSymbol investingSymbol; 	// код бумаги на investing.com
	
	int timeframe; // in seconds
	List<InvestingCall> requests = new LimitedArrayList<>(100);
	
	SignalProcessor signalProcessor;

	public InvestingSecurity(TQSymbol symbol, InvestingSymbol investingSymbol, int operQuantity, int timeframe) {
		this.symbol = symbol;
		this.investingSymbol = investingSymbol;
		this.timeframe = timeframe;
		this.signalProcessor = new SignalProcessor(symbol, operQuantity);
	}
		
	public void processRequest(InvestingCall investingRequest) {
		InvestingCall current = investingRequest;
		logger.debug(symbol + " " + current.toString());
		if (requests.size() > 0) {
			InvestingCall last = requests.get(requests.size()-1);
			if (last.signal != current.signal) {
				InvestingSignal investingSignal = new InvestingSignal(investingSymbol, current.price, timeframe, current.signal);
				signalProcessor.processSignal(investingSignal);
			}
		}
		requests.add(current);
	}
	
	public void onStart() {
		
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

	public int getRequestPeriod() {
		return timeframe;
	}

	public InvestingSymbol getInvestingSymbol() {
		return investingSymbol;
	}

}
