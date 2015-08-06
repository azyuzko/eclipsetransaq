package ru.eclipsetrader.transaq.core.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.strategy.Strategy.WorkOn;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class StrategyTester {

	static class StrategyInstance implements Callable<Holder<Double, String>> {
		
		Strategy macd;
		DataFeeder dataFeeder;
		int index;
		
		public StrategyInstance(int index, Strategy macd, DataFeeder dataFeeder) {
			this.index = index;
			this.macd = macd;
			this.dataFeeder = dataFeeder;
		}

		@Override
		public Holder<Double, String> call() {
		
			dataFeeder.feed(macd);

			macd.closePositions();

			IAccount account = macd.getAccount();			
			Double free = account.getFree();
			String desc = index + " FREE: " + free + " " + macd + ": " + account.toString();
			System.out.println(Thread.currentThread().getName() + " Complete " + desc );
			this.macd = null;
			return new Holder<Double, String>(free, desc);
		}
		
	}
	

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 11:15:00.000");
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate, TQSymbol.workingSymbolSet().toArray(new TQSymbol[0])); // new TQSymbol[] {TQSymbol.RTSI, TQSymbol.SiU5});
		
		ExecutorService service = Executors.newFixedThreadPool(3);

		List<Future<Holder<Double, String>>> lr = new ArrayList<>();
		
		List<Holder<TQSymbol, TQSymbol>> symbolList = new ArrayList<Holder<TQSymbol,TQSymbol>>();
		
		for (TQSymbol key : TQSymbol.workingSymbolSet()) {
			for (TQSymbol value : TQSymbol.workingSymbolSet()) {
				if (!key.equals(value) 
						&& !value.equals(TQSymbol.RTSI) && !value.equals(TQSymbol.RTS2)
					) {
					symbolList.add(new Holder<TQSymbol, TQSymbol>(key, value));
				}
			}
		}
		
		// symbolList.add(new Holder<TQSymbol, TQSymbol>(TQSymbol.RTSI, TQSymbol.SiU5));

		int index = 0;
		/*for (int i = 4; i < 7; i++) {
			for (int j = 10; j < 16; j++) {
				for (int x = i+3; x < j; x++) {*/
					for (Holder<TQSymbol, TQSymbol> symbols : symbolList) {
						for (WorkOn workOn : WorkOn.values()) {
							for (CandleType candleType : new CandleType[] {CandleType.CANDLE_10S, CandleType.CANDLE_15S, CandleType.CANDLE_30S, CandleType.CANDLE_1M} ) {
								for (PriceType priceType : new PriceType[] {PriceType.CLOSE, PriceType.WEIGHTED_CLOSE}) {
									Strategy macd = new Strategy(dataFeeder, 6, 12, 9, priceType, symbols.getFirst(), symbols.getSecond(), workOn, candleType);
									StrategyInstance s = new StrategyInstance(index, macd, dataFeeder);
									Future<Holder<Double, String>> fut = service.submit(s);
									lr.add(fut);
									index++;
								}
							}
						}
					}
/*				}
			}
		}*/
		
		System.out.println("****** Jobs size = " + lr.size());

		TreeMap<Double, String> sorted = new TreeMap<>();
		
		for (Future<Holder<Double, String>> f : lr) {
			Holder<Double, String> result = f.get();
			sorted.put(result.getFirst(), result.getSecond());
		}
		
		System.out.println("sorted size = " + sorted.size());
		
		for (Double free : sorted.descendingKeySet()) {
			System.out.println("Free: " + free + " = " + sorted.get(free) );
		}
		
		service.shutdown();
		
	}

}
