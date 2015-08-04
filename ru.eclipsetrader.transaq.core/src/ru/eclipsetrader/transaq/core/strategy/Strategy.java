package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Strategy implements IProcessingContext, IStrategy {

	Logger logger = LogManager.getLogger("MACDStrategy");
	
	private MACD macd;

	public Instrument BRQ5;
	public Instrument SiU5;
	public Instrument RIU5;
	
	IDataFeedContext dataFeedContext;
	
	public Strategy(IDataFeedContext dataFeedContext, int fast, int slow, int signal) {
		this.dataFeedContext = dataFeedContext;
		macd = new MACD(fast, slow, signal);
		BRQ5 = new Instrument(TQSymbol.BRQ5, this, dataFeedContext);
		SiU5 = new Instrument(TQSymbol.SiU5, this, dataFeedContext);
		RIU5 = new Instrument(TQSymbol.RIU5, this, dataFeedContext);		
	}
	
	public void reset() {
		BRQ5.reset();
		SiU5.reset();
		RIU5.reset();
	}

	public MACD getMacd() {
		return macd;
	}

	public void setMacd(MACD macd) {
		this.macd = macd;
	}

	public void tick(Instrument i) {
		Holder<Date[], double[]> values = i.getCandleStorage().getCandleList(CandleType.CANDLE_1M).values(PriceType.CLOSE);
		Date[] dates = values.getFirst();
		double[] inReal = values.getSecond();
		macd.evaluate(inReal, dates);
		
		double[] m = macd.getOutMACD();
		double[] signal = macd.getOutMACDSignal();
		double[] hist = macd.getOutMACDHist();
		if (hist.length > macd.getLookback()) {
			if (Math.signum(hist[hist.length-1]) != Math.signum(hist[hist.length-2])) {
				BuySell bs;
				if (Math.signum(hist[hist.length-1]) == -1) {
					bs = BuySell.B;
				} else {
					bs = BuySell.S;
				}
				if (i == RIU5) {
					signal(SiU5, bs);
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

	private void createSignal(Instrument i, BuySell buySell) {
		signals.put(i.getSymbol(), new Signal(i.getSymbol(), dataFeedContext.currentDate(), buySell, 0));
		int quantity = 100;
		if (buySell == BuySell.B) {
			quantity = i.buy(quantity).getQuantity();
		} else {
			quantity = i.sell(quantity).getQuantity();
		}
		// System.out.println(i.getSymbol() +" signal " + Utils.formatDate(date) + " " + buySell + " = " + quantity);
		
	}
	
	HashMap<TQSymbol, Signal> signals = new HashMap<>();
	
	public void signal(Instrument i, BuySell buySell) {
		
		if (signals.size() == 0) {
			createSignal(i, buySell);
		} else {
			if (signals.get(i.getSymbol()) != null && signals.get(i.getSymbol()).getBuySell() != buySell) {
				createSignal(i, buySell);
			}
		}
		
	}


	@Override
	public void start() {
		reset();
		dataFeedContext.OnStart();
	}

	@Override
	public void stop() {
		
	}


	@Override
	public void onTick(Instrument instrument, Tick tick) {
//		System.out.println(tick);
		tick(instrument);
	}

	@Override
	public void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass) {
		// System.out.println("OnQuotesChange: " + instrument.getSymbol() );
	}

	@Override
	public void onCandleClose(Instrument instrument, CandleList candleList, Candle candle) {
		//System.out.println("onCandleClose: " + instrument.getSymbol() + " " + candle.toString());
	}

	@Override
	public void onCandleOpen(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleOpen: " + instrument.getSymbol() + " " + candle.toString());
		
	}

	@Override
	public void onCandleChange(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleChange: " + instrument.getSymbol() + " " + candle.toString());
	}
	

	@Override
	public void onQuotationsChange(Instrument instrument, Quotation quotation) {
		// System.out.println("Quotations changed");
	}

	@Override
	public CandleType[] getCandleTypes() {
		return new CandleType[] { CandleType.CANDLE_1M };
	}
	

	@Override
	public TQSymbol[] getSymbols() {
		return new TQSymbol[] {SiU5.getSymbol(), BRQ5.getSymbol(), RIU5.getSymbol()} ;
	}


	@Override
	public IDataFeedContext getDataFeedContext() {
		return dataFeedContext;
	}

	@Override
	public IProcessingContext getProcessingContext() {
		return this;
	}

	@Override
	public Instrument getInstrument(TQSymbol symbol) {
		if (symbol.equals(TQSymbol.SiU5)) {
			return SiU5;
		} else if (symbol.equals(TQSymbol.BRQ5)) {
			return BRQ5;
		} else if (symbol.equals(TQSymbol.RIU5)) {
			return RIU5;
		}
		return null;
	}

	@Override
	public String getName() {
		return "MACD strategy fast = " + macd.getOptInFastPeriod() + ", slow = " + macd.getOptInSlowPeriod() + ", signal = " + macd.getOptInSignalPeriod();
	}

	
}
