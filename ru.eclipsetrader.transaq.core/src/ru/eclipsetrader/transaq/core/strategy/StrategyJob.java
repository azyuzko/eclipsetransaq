package ru.eclipsetrader.transaq.core.strategy;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.account.SimpleAccount;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.trades.DataFeeder.FeedType;
import ru.eclipsetrader.transaq.core.util.Holder;

public class StrategyJob implements Callable<Holder<Double, String>> {
	
	Logger logger = LogManager.getLogger("StrategyJob");

	static final double INITIAL_AMOUNT = 100000;
	
	Strategy strategy;
	DataFeeder dataFeeder;
	int index;
	
	public StrategyJob(int index, Strategy macd, DataFeeder dataFeeder) {
		this.index = index;
		this.strategy = macd;
		this.dataFeeder = dataFeeder;
	}

	@Override
	public Holder<Double, String> call() {
	
		HashMap<TQSymbol, QuantityCost> initPositions = new HashMap<>();
		initPositions.put(TQSymbol.SiU5, new QuantityCost(1, 0));
		SimpleAccount account = new SimpleAccount(INITIAL_AMOUNT, strategy, initPositions);
		
		dataFeeder.feed(FeedType.TICKS, strategy, account);
		
		Double free = account.getFree();
		String desc = String.format("%d FREE: %f %s: %s", index, free, strategy.toString(), account.toString());
		logger.info("Complete " + desc );
		this.strategy = null;
		return new Holder<Double, String>(free, desc);
	}
	
}
