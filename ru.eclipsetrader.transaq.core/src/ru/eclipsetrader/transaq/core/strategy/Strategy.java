package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tictactec.ta.lib.MAType;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MA;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.StrategyWorkOn;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Strategy extends StrategyParamsType implements IProcessingContext, IStrategy {
	


	Logger logger = LogManager.getLogger("Strategy");
	
	private MACD macd;

	public Instrument iWatch;
	public Instrument iOper;
	
	IDataFeedContext dataFeedContext;
	IAccount account;
	Date currentDate = null;
	
	public Strategy(IDataFeedContext dataFeedContext, StrategyParamsType params) {
		super(params);
		this.dataFeedContext = dataFeedContext;
		this.macd = new MACD(fast, slow, signal);
		this.iWatch = new Instrument(watchSymbol, this, dataFeedContext);
		if (operSymbol != null) {
			this.iOper = new Instrument(operSymbol, this, dataFeedContext);
		}
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
			logger.debug("working on " + i.getSymbol());
			PriceType _pt = priceType;
			CandleType _ct = candleType;	
			
			Holder<Date[], double[]> values = i.getCandleStorage().getCandleList(_ct).values(_pt);
			Date[] dates = values.getFirst();
			double[] inReal = values.getSecond();
			macd.evaluate(inReal, dates);
			
			double[] m = macd.getOutMACD();
			double[] signal = macd.getOutMACDSignal();
			double[] hist = macd.getOutMACDHist();
			if (hist.length > macd.getLookback()) {
				logger.debug("hist " + hist[hist.length-1] + " -- " + hist[hist.length-2]);
				if (Math.signum(hist[hist.length-1]) != Math.signum(hist[hist.length-2])) {
					BuySell bs;
					if (Math.signum(hist[hist.length-1]) == -1) {
						bs = BuySell.B;
					} else {
						bs = BuySell.S;
					}
					Instrument iSignal = (iOper != null ? iOper : iWatch);
					signal(iSignal, bs, values.getSecond()[values.getSecond().length-1]);
				}
			} else {
				logger.debug("Not enough history length = " + hist.length + " for lookback " + macd.getLookback());
			}
		}
	}

	public void print(Date[] dates, double[] macd, double[] macdSignal, double[] macdHist) {
		print(dates.length, macd, macdSignal, macdHist);
	}
	
	public void print(int lastCount, Date[] dates, double[] macd, double[] macdSignal, double[] macdHist) {
		logger.info("date   :" + Utils.printArray(macd.getDates(lastCount), "%6tR"));
		logger.info("macd   :" + Utils.printArray(macd.getOutMACD(lastCount), "%6.2f"));
		logger.info("signal :" + Utils.printArray(macd.getOutMACDSignal(lastCount), "%6.2f"));
		logger.info("hist   :" + Utils.printArray(macd.getOutMACDHist(lastCount), "%6.2f"));
		
		for (Signal signal : signals.values()) {
			logger.info(signal);
		}
		
	}
	
	Lock signalLock = new ReentrantLock();

	int quantity = 4;
	boolean firstPos = true;
	
	private void createSignal(Instrument i, BuySell buySell, double price) {
		if (signalLock.tryLock()) {
			try {
				logger.info("createSignal " +i.getSymbol() + " " + buySell + " price = " + price);
				
				int result = 0;
				if (buySell == BuySell.B) {
					if (firstPos) {
						result = i.buy(quantity / 2).getQuantity();
					} else {
						result = i.buy(quantity).getQuantity();
					}
					
				} else {
					if (firstPos) {
						result = i.sell(quantity / 2).getQuantity();
					} else {
						result = i.sell(quantity).getQuantity();
					}
				}
				if (result > 0) {
					signals.put(i.getSymbol(), new Signal(i.getSymbol(), getDateTime(), buySell, price));
					logger.info("Executed " +i.getSymbol() +" signal " + Utils.formatDate(getDateTime()) + " " + buySell + " = " + quantity + " result = " + result);
					print(slow);
				}
				firstPos = false;
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				signalLock.unlock();
			}
		} else {
			logger.debug("Already locked for another buysell operation on " + i.getSymbol());
		}
	}
	
	HashMap<TQSymbol, Signal> signals = new HashMap<>();
	
	public void signal(Instrument i, BuySell buySell, double price) {
		logger.debug("signal " + i.getSymbol() + " " + buySell);
		if (signals.size() == 0) {
			createSignal(i, buySell, price);
		} else {
			if (signals.get(i.getSymbol()) != null && signals.get(i.getSymbol()).getBuySell() != buySell) {
				createSignal(i, buySell, price);
			}
		}
		
	}


	@Override
	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		if (iOper != null) {
			dataFeedContext.OnStart(new Instrument[] { iWatch, iOper });
		} else {
			dataFeedContext.OnStart(new Instrument[] { iWatch });
		}
		logger.debug("Started.");
	}

	@Override
	public void stop() {
		logger.debug("Stopped");
	}


	@Override
	public void onTick(Instrument instrument, Tick tick) {
		//System.out.println("onTick: " + instrument.getSymbol() + " " + tick.getTime());
		
	}

	@Override
	public void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass) {
		// System.out.println(instrument.getSymbol() + " on Quotes Change");
	}

	@Override
	public void onCandleClose(Instrument instrument, CandleList candleList, Candle candle) {
		logger.debug("onCandleClose: " + instrument.getSymbol() + " " + candle.toString());
		if (workOn == StrategyWorkOn.CandleClose) {
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
		if (workOn == StrategyWorkOn.CandleChange) {
			tick(instrument);
		}
	}
	

	@Override
	public void onQuotationsChange(Instrument instrument, Quotation quotation) {
		
	}

	@Override
	public CandleType[] getCandleTypes() {
		return new CandleType[] { candleType };
		//return CandleType.values();
	}
	

	@Override
	public TQSymbol[] getSymbols() {
		if (iOper.getSymbol() != null) {
			return new TQSymbol[] {iWatch.getSymbol(), iOper.getSymbol() } ;
		} else {
			return new TQSymbol[] {iWatch.getSymbol() } ;
		}
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
		return "STR fast= " + macd.getOptInFastPeriod() + ", slow= " + macd.getOptInSlowPeriod() + ", signal= " + macd.getOptInSignalPeriod() + " iWatch= " + (iWatch != null ? iWatch.getSymbol() : "null") + " iOper= " + (iOper != null ? iOper.getSymbol() : "null") + " " + workOn + " " +candleType + " " + StringUtils.leftPad(priceType.toString(), 15);
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
