package ru.eclipsetrader.transaq.core.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class MACDStrategy implements IProcessingContext {

	private MACD macd;
	private MACDStrategyProperties properties;

	Account account = new Account(1000000, 0); // 100 000 руб
	
	TreeMap<Date, Signal> signals = new TreeMap<>();
	
	public MACDStrategy(MACDStrategyProperties properties) {
		this.macd = new MACD(properties.optInFastPeriod, properties.optInSlowPeriod, properties.optInSignalPeriod);
		this.properties = properties;
	}
	
	public MACD getMacd() {
		return macd;
	}
	
	public Account getAccount() {
		return account;
	}

	@Override
	public void completeTrade(TickTrade tick, Instrument i) {
		CandleList cl = i.getCandleStorage().getCandleList(properties.candleType);
		double[] inReal = cl.values(properties.priceType);
		Candle lastCandle = cl.getLastCandle();
		Date[] dates = cl.dates();
		macd.evaluate(inReal, dates);
		
		double[] m = macd.getOutMACD();
		double[] signal = macd.getOutMACDSignal();
		double[] hist = macd.getOutMACDHist();
		if (hist.length > macd.getLookback()) {
			if (Math.signum(hist[hist.length-1]) != Math.signum(hist[hist.length-2])) {
				if (Math.signum(hist[hist.length-1]) == -1) {
					signal(dates[dates.length-1], BuySell.B, tick.getPrice());
				} else {
					signal(dates[dates.length-1], BuySell.S, tick.getPrice());
				}
			}
		}
		
	}
	
	@Override
	public void complete(Instrument i) {
		double price = i.getCandleStorage().getCandleList(properties.candleType).getLastCandle().getClose();
		account.close(price, price);
	}
	
	public void print() {
		print(macd.getDates().length);
	}
	
	public void print(int lastCount) {
		System.out.println("date   :" + Utils.printArray(macd.getDates(lastCount), "%6tR"));
		System.out.println("macd   :" + Utils.printArray(macd.getOutMACD(lastCount), "%6.2f"));
		System.out.println("signal :" + Utils.printArray(macd.getOutMACDSignal(lastCount), "%6.2f"));
		System.out.println("hist   :" + Utils.printArray(macd.getOutMACDHist(lastCount), "%6.2f"));
		
		for (Signal signal : signals.values()) {
			System.out.println(signal);
		}
		
		System.out.println("Account: " + account);
		
	}

	private void createSignal(Date date, BuySell buySell, double price) {
		int quantity = account.available(buySell, price);
		
		if (quantity != 0) {
			Signal signal = new Signal(date, buySell, price);
			signal.setQuantity(quantity);
			signals.put(date, signal);
			account.buysell(buySell, price, quantity);
			// System.out.println(Utils.formatDate(date) + " signal " + buySell + " price = " + price);
		}
	}
	
	public void signal(Date date, BuySell buySell, double price) {
		
		if (signals.size() == 0) {
			createSignal(date, buySell, price);
		} else {
			if (signals.lastEntry().getValue().getBuySell() != buySell) {
				createSignal(date, buySell, price);
			}
		}
		
	}

	@Override
	public void completeCandle(Candle candle) {
		// TODO Auto-generated method stub
		
	}
	
	static List<MACDStrategy> results = Collections.synchronizedList(new ArrayList<MACDStrategy>());

	public static void main(String[] args) throws IOException {
		CandleType candleType = CandleType.CANDLE_5M;
		PriceType priceType = PriceType.CLOSE;
		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		
		List<TickTrade> list = DataFeeder.getTradeList(symbol, Utils.parseDate("20.07.2015 00:00:00.000"), Utils.parseDate("21.07.2015 23:59:00.000"));

		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		for (int fast = 3; fast < 15; fast++) {
			for (int slow = fast + 3; slow < fast*2; slow++) {
				for (int signal = fast-2; signal < fast+3; signal++) {
					MACDStrategyProperties props = new MACDStrategyProperties();
					props.candleType = candleType;
					props.priceType = priceType;
					props.optInFastPeriod = fast;
					props.optInSlowPeriod = slow;
					props.optInSignalPeriod = signal;
					
					pool.submit(new MACDStrategyFeederTask(symbol, list, props));
					
					//			macdStrategy.print();
				}
			}
		}
		
		System.in.read();
		pool.shutdown();
		
		Collections.sort(results, new Comparator<MACDStrategy>() {
			@Override
			public int compare(MACDStrategy o1, MACDStrategy o2) {
				return Double.compare(o1.getAccount().getCash(), o2.getAccount().getCash());
			}
		});
		
		
		for (MACDStrategy macdStrategy : results) {
			MACD macd = macdStrategy.macd;
			System.out.println("fast = " + macd.getOptInFastPeriod() + " slow = " + macd.getOptInSlowPeriod() + " signal = " + macd.getOptInSignalPeriod() + " signal_count = " + macdStrategy.signals.size() + " **** result = " + macdStrategy.getAccount().toString());
		}

		System.out.println("-- print last success ");
		results.get(results.size()-1).print();
		
		System.out.println("Done!");
	}


	
}
