package ru.eclipsetrader.transaq.core.strategy;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.util.Holder;

public class StrategyJob implements Callable<Holder<Double, String>> {
	
	Logger logger = LogManager.getLogger("StrategyJob");
	
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
	
		dataFeeder.feed(strategy);
		
		IAccount account = strategy.getAccount();
		Double free = account.getFree();
		String desc = String.format("%d FREE: %f %s: %s", index, free, strategy.toString(), account.toString());
		logger.info("Complete " + desc );
		this.strategy = null;
		return new Holder<Double, String>(free, desc);
	}
	
}
