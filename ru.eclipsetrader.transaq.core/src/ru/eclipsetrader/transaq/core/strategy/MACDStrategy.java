package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;
import java.util.HashMap;

import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Utils;

public class MACDStrategy implements IProcessingContext, IStrategy {

	private MACD macd;

	public Instrument BRQ5;
	public Instrument SiU5;
	public Instrument RIU5;
	
	IDataFeedContext dataFeedContext;
	
	public MACDStrategy(IDataFeedContext dataFeedContext) {
		this.dataFeedContext = dataFeedContext;
		macd = new MACD(12, 26, 9);
		BRQ5 = new Instrument(TQSymbol.BRQ5, this, dataFeedContext);
		SiU5 = new Instrument(TQSymbol.SiU5, this, dataFeedContext);
		RIU5 = new Instrument(TQSymbol.RIU5, this, dataFeedContext);
	}

	public MACD getMacd() {
		return macd;
	}


	public void tick(Instrument i, CandleList candleList) {
		double[] inReal = candleList.values(PriceType.CLOSE);
		Date[] dates = candleList.dates();
		macd.evaluate(inReal, dates);
		
		double[] m = macd.getOutMACD();
		double[] signal = macd.getOutMACDSignal();
		double[] hist = macd.getOutMACDHist();
		if (hist.length > macd.getLookback()) {
			if (Math.signum(hist[hist.length-1]) != Math.signum(hist[hist.length-2])) {
				if (Math.signum(hist[hist.length-1]) == -1) {
					signal(i, dates[dates.length-1], BuySell.B);
				} else {
					signal(i, dates[dates.length-1], BuySell.S);
				}
			}
		}
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
		
	}

	private void createSignal(Instrument i, Date date, BuySell buySell) {
		System.out.println(i.getSymbol() +" signal " + date + " " + buySell);
		signals.put(i.getSymbol(), new Signal(i.getSymbol(), date, buySell, 0));
	}
	
	HashMap<TQSymbol, Signal> signals = new HashMap<>();
	
	public void signal(Instrument i, Date date, BuySell buySell) {
		
		if (signals.size() == 0) {
			createSignal(i, date, buySell);
		} else {
			if (signals.get(i.getSymbol()) != null && signals.get(i.getSymbol()).getBuySell() != buySell) {
				createSignal(i, date, buySell);
			}
		}
		
	}


	@Override
	public void start() {
		dataFeedContext.OnStart();
	}

	@Override
	public void stop() {
		
	}


	@Override
	public void onTick(Instrument instrument, Tick tick) {
//		System.out.println(tick);
	}

	@Override
	public void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass) {
		// System.out.println("OnQuotesChange: " + instrument.getSymbol() );
	}

	@Override
	public void onCandleClose(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleClose: " + instrument.getSymbol() + " " + candle.toString());
	}

	@Override
	public void onCandleOpen(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleOpen: " + instrument.getSymbol() + " " + candle.toString());
	}

	@Override
	public void onCandleChange(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleChange: " + instrument.getSymbol() + " " + candle.toString());
		tick(instrument, candleList);
	}

	@Override
	public CandleType[] getCandleTypes() {
		return new CandleType[] { CandleType.CANDLE_1M };
	}

	@Override
	public IDataFeedContext getDataFeedContext() {
		return dataFeedContext;
	}

	@Override
	public IProcessingContext getProcessingContext() {
		return this;
	}
	
}
