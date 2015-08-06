package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
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
	
	public static enum WorkOn {
		CandleClose,
		Tick,
	}

	Logger logger = LogManager.getLogger("Strategy");
	
	private MACD macd;

	public Instrument iWatch;
	public Instrument iOper;
	
	IDataFeedContext dataFeedContext;
	IAccount account;
	PriceType priceType;
	WorkOn workOn;
	CandleType candleType;
	Date currentDate = null;
	
	public Strategy(IDataFeedContext dataFeedContext, int fast, int slow, int signal, PriceType priceType, TQSymbol iWatch, TQSymbol iOper, WorkOn workOn, CandleType candleType) {
		this.dataFeedContext = dataFeedContext;
		this.priceType = priceType;
		this.macd = new MACD(fast, slow, signal);
		this.candleType = candleType; // before create instruments
		this.iWatch = new Instrument(iWatch, this, dataFeedContext);
		this.iOper = new Instrument(iOper, this, dataFeedContext);
		this.workOn = workOn;
	}

	@Override
	public void setDateTime(Date date) {
		currentDate = date;
	}


	@Override
	public Date getDateTime() {
		if (currentDate == null) {
			return new Date();
		} else {
			return currentDate;
		}
	}

	public MACD getMacd() {
		return macd;
	}

	public void setMacd(MACD macd) {
		this.macd = macd;
	}

	public void tick(Instrument i) {
		if (i == iWatch) {
			Holder<Date[], double[]> values = i.getCandleStorage().getCandleList(candleType).values(priceType);
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
					signal(iOper, bs);
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
		int quantity = 1000;
		int result = 0;
		if (buySell == BuySell.B) {
			result = i.buy(quantity).getQuantity();
		} else {
			result = i.sell(quantity).getQuantity();
		}
		if (result > 0) {
			signals.put(i.getSymbol(), new Signal(i.getSymbol(), getDateTime(), buySell, 0));
			logger.info("Executed " +i.getSymbol() +" signal " + Utils.formatDate(getDateTime()) + " " + buySell + " = " + quantity + " result = " + result);
		}
	}
	
	HashMap<TQSymbol, Signal> signals = new HashMap<>();
	
	public void signal(Instrument i, BuySell buySell) {
		logger.debug("signal " + i.getSymbol() + " " + buySell);
		if (signals.size() == 0) {
			createSignal(i, buySell);
		} else {
			if (signals.get(i.getSymbol()) != null && signals.get(i.getSymbol()).getBuySell() != buySell) {
				createSignal(i, buySell);
			}
		}
		
	}


	@Override
	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		dataFeedContext.OnStart(new Instrument[] { iOper, iWatch });
		logger.debug("Started.");
	}

	@Override
	public void stop() {
		logger.debug("Stopped");
	}


	@Override
	public void onTick(Instrument instrument, Tick tick) {
		//System.out.println("onTick: " + instrument.getSymbol() + " " + tick.getTime());
		if (workOn == WorkOn.Tick) {
			tick(instrument);
		}
	}

	@Override
	public void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass) {
		// System.out.println(instrument.getSymbol() + " on Quotes Change");
	}

	@Override
	public void onCandleClose(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleClose: " + instrument.getSymbol() + " " + candle.toString());
		if (workOn == WorkOn.CandleClose) {
			tick(instrument);
		}
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
		
	}

	@Override
	public CandleType[] getCandleTypes() {
		return new CandleType[] { candleType };
	}
	

	@Override
	public TQSymbol[] getSymbols() {
		return new TQSymbol[] {iWatch.getSymbol(), iOper.getSymbol() } ;
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
		if (symbol.equals(iWatch.getSymbol())) {
			return iWatch;
		} else if (symbol.equals(iOper.getSymbol())) {
			return iOper;
		}
		return null;
	}

	@Override
	public String toString() {
		return "STR fast= " + macd.getOptInFastPeriod() + ", slow= " + macd.getOptInSlowPeriod() + ", signal= " + macd.getOptInSignalPeriod() + " iWatch= " + (iWatch != null ? iWatch.getSymbol() : "null") + " iOper= " + (iOper != null ? iOper.getSymbol() : "null") + " " + workOn + " " +candleType + " " + priceType;
	}

	@Override
	public void closePositions() {
		Map<TQSymbol, QuantityCost> positions = account.getPositions();
		logger.debug("Close positions size " + positions.size());
		for (TQSymbol symbol : positions.keySet()) {
			logger.debug("Closing position " + symbol + " = " + positions.get(symbol));
			Instrument i = getInstrument(symbol);
			if (i != null) {
				QuantityCost toSell = positions.get(symbol);
				QuantityCost sold = i.sell(toSell.getQuantity());
				if (sold.getQuantity() < toSell.getQuantity()) {
					logger.error("Position cannot be closed! toSell = " + toSell + ",   sold = " + sold);
					i.sell(toSell.getQuantity());
				} else {
					logger.debug("Closed " +symbol + "position " + sold);
				}
			} else{
				throw new RuntimeException("Instrument " + symbol + " not found");
			}
		}
	}

	@Override
	public IAccount getAccount() {
		return account;
	}


	
}
