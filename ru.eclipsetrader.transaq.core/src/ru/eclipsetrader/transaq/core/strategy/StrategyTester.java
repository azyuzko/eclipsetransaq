package ru.eclipsetrader.transaq.core.strategy;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class StrategyTester {
	
	final static ConcurrentSkipListMap<Double, String> results = new ConcurrentSkipListMap<>(); 
	
	static class StrategyInstance implements Runnable {
		
		Strategy macd;
		DataFeeder dataFeeder;
		
		public StrategyInstance(Strategy macd, DataFeeder dataFeeder) {
			this.macd = macd;
			this.dataFeeder = dataFeeder;
		}

		@Override
		public void run() {
			dataFeeder.feed(macd);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			IAccount account = dataFeeder.getAccount();
			Map<TQSymbol, QuantityCost> positions = account.getPositions();
			for (TQSymbol symbol : positions.keySet()) {
				macd.getInstrument(symbol).sell(positions.get(symbol).getQuantity());
			}
			
			Double free = account.getFree();
			String name = macd.getName() + ": " + account.toString();
			System.out.println("Completed " + name);
			results.put(free, name);
			this.macd = null;
		}
		
	}
	

	public static void main(String[] args) throws IOException {
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate);
		
		ExecutorService service = Executors.newFixedThreadPool(10);
		
		for (int i = 2; i < 10; i++) {
			for (int j = i; j < i + 10; j++) {
				for (int x = 2; x < j; x++) {
					Strategy macd = new Strategy(dataFeeder, i, j, x);
					StrategyInstance s = new StrategyInstance(macd, dataFeeder);
					service.execute(s);
				}
			}
		}
		
	/*	
		System.out.println(account);
		*/

		

		//System.out.println("Done!");
		// System.in.read();
	}

}
