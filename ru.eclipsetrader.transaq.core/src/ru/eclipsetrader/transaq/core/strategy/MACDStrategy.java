package ru.eclipsetrader.transaq.core.strategy;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

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
import ru.eclipsetrader.transaq.core.util.Utils;

public class MACDStrategy implements IProcessingContext {

	private MACD macd;
	FortsAccount account = new FortsAccount(1000000); // 100 000 руб
	
	TreeMap<Date, Signal> signals = new TreeMap<>();
	
	public MACDStrategy() {
		this.macd = new MACD(7, 15, 5);
	}
	
	public MACD getMacd() {
		return macd;
	}
	
	public FortsAccount getAccount() {
		return account;
	}

	@Override
	public void completeTrade(TickTrade tick, Instrument i) {
		CandleList cl = i.getCandleStorage().getCandleList(CandleType.CANDLE_10M);
		double[] inReal = cl.values(PriceType.CLOSE);
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
		double price = i.getCandleStorage().getCandleList(CandleType.CANDLE_10M).getLastCandle().getClose();
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

	public static void main(String[] args) throws IOException {
		CandleType candleType = CandleType.CANDLE_5M;
		PriceType priceType = PriceType.CLOSE;
		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");

		
		System.out.println("Done!");
	}

	@Override
	public CandleType[] getCandleTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
