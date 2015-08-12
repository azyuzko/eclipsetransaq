package ru.eclipsetrader.transaq.core.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
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
	
	private MACD macdBr;
	private MACD macdSi;

	public Instrument iBR;
	public Instrument iRI;
	public Instrument iSi;
	
	IDataFeedContext dataFeedContext;
	IAccount account;
	Date currentDate = null;
	
	public Strategy(IDataFeedContext dataFeedContext, StrategyParamsType params) {
		super(params);
		this.dataFeedContext = dataFeedContext;
		this.macdBr = new MACD(6, 12, 9);
		this.macdSi = new MACD(fast, slow, signal);
		this.iBR = new Instrument(TQSymbol.BRQ5, this, dataFeedContext);
		this.iSi = new Instrument(TQSymbol.SiU5, this, dataFeedContext);
		this.iRI = new Instrument(TQSymbol.RIU5, this, dataFeedContext);
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
		return macdBr;
	}

	public void setMacd(MACD macd) {
		this.macdBr = macd;
	}

	public void tick(Instrument i) {
		if (i.getSymbol().equals(TQSymbol.BRQ5)) {
			logger.debug("working on " + i.getSymbol());
			PriceType _pt = priceType;
			CandleType _ct = candleType;	
			
			Holder<Date[], double[]> valuesBR = iBR.getCandleStorage().getCandleList(CandleType.CANDLE_16S).values(PriceType.CLOSE);
			Holder<Date[], double[]> valuesSi = iSi.getCandleStorage().getCandleList(_ct).values(_pt);
			macdBr.evaluate(valuesBR.getSecond(), valuesBR.getFirst());
			macdSi.evaluate(valuesSi.getSecond(), valuesSi.getFirst());
			
			double[] histBR = macdBr.getOutMACDHist();
			if (histBR.length > macdBr.getLookback()) {
				if ((Math.signum(histBR[histBR.length-1]) != Math.signum(histBR[histBR.length-2]))
						&& (Math.abs(histBR[histBR.length-2]) < Math.abs(histBR[histBR.length-3]))
						&& (Math.signum(histBR[histBR.length-2]) == Math.signum(histBR[histBR.length-3]))
						&& (Math.abs(histBR[histBR.length-1]) > 0.0005)
						) {
					BuySell bs;
					if (Math.signum(histBR[histBR.length-1]) == -1) {
						bs = BuySell.B;
					} else {
						bs = BuySell.S;
					}
					signal(iSi, bs, valuesBR, valuesSi);
				}
			} else {
				logger.debug("Not enough history length = " + histBR.length + " for lookback " + macdBr.getLookback());
			}
		}
	}
	
	private Object[] last(Object[] values, int lastCount) {
		return ArrayUtils.subarray(values, values.length - lastCount, values.length);
	}

	private double[] last(double[] values, int lastCount) {
		return ArrayUtils.subarray(values, values.length - lastCount, values.length);
	}
	
	
	Lock signalLock = new ReentrantLock();

	int quantity = 1;
	boolean firstPos = true;
	
	private void createSignal(Instrument i, BuySell buySell, Holder<Date[], double[]> valuesBR, Holder<Date[], double[]> valuesSi) {
		if (signalLock.tryLock()) {
			try {
				double priceBR = valuesBR.getSecond()[valuesBR.getSecond().length-1];
				double priceSi = valuesSi.getSecond()[valuesSi.getSecond().length-1];
				logger.info("createSignal " +i.getSymbol() + " " + buySell + " BR = " + priceBR + ", Si = " + priceSi);
				
				int result = 0;
				if (buySell == BuySell.B) {
					if (firstPos) {
						result = i.buy(quantity).getQuantity();
					} else {
						result = i.buy(quantity).getQuantity();
					}
					
				} else {
					if (firstPos) {
						result = i.sell(quantity).getQuantity();
					} else {
						result = i.sell(quantity).getQuantity();
					}
				}
				if (result > 0) {
					signals.put(i.getSymbol(), new Signal(i.getSymbol(), getDateTime(), buySell, priceBR));
					
					StringBuilder sb = new StringBuilder();
					sb.append("\n");
					sb.append("date      :" + Utils.printArray(last(valuesBR.getFirst(), 20), "%7tR") + "\n");
					sb.append("Br prices :" + Utils.printArray(last(valuesBR.getSecond(), 20), "%7.2f") + "\n");
					sb.append("BR hist   :" + Utils.printArray(last(macdBr.getOutMACDHist(), 20), "%7.4f") + "\n");
					sb.append("Si price  :" + Utils.printArray(last(valuesSi.getSecond(), 20), "%7.0f") + "\n");
					sb.append("Si hist   :" + Utils.printArray(last(macdSi.getOutMACDHist(), 20), "%7.2f") + "\n");
					
					logger.info("Executed " +i.getSymbol() +" signal " + Utils.formatDate(getDateTime()) + " " + buySell + " = " + quantity + " result = " + result);
					logger.info(sb.toString());
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
	
	public void signal(Instrument i, BuySell buySell, Holder<Date[], double[]> valuesBR, Holder<Date[], double[]> valuesSi) {
		logger.debug("signal " + i.getSymbol() + " " + buySell);
		if (signals.size() == 0) {
			createSignal(i, buySell, valuesBR, valuesSi);
		} else {
			if (signals.get(i.getSymbol()) != null && signals.get(i.getSymbol()).getBuySell() != buySell) {
				createSignal(i, buySell, valuesBR, valuesSi);
			}
		}
		
	}

	@Override
	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		dataFeedContext.OnStart(new Instrument[] { iBR, iSi, iRI });
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
		return new CandleType[] { CandleType.CANDLE_16S, candleType };
		//return CandleType.values();
	}
	

	@Override
	public TQSymbol[] getSymbols() {
		return new TQSymbol[] {iBR.getSymbol(), iSi.getSymbol(), iRI.getSymbol() } ;
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
		if (symbol.equals(iBR.getSymbol())) {
			return iBR;
		} else if (symbol.equals(iSi.getSymbol())) {
			return iSi;
		} else if (symbol.equals(iRI.getSymbol())) {
			return iRI;
		}
		return null;
	}

	@Override
	public String toString() {
		return "STR fast= " + macdSi.getOptInFastPeriod() + ", slow= " + macdSi.getOptInSlowPeriod() + ", signal= " + macdSi.getOptInSignalPeriod() + " iWatch= " + (iBR != null ? iBR.getSymbol() : "null") + " iOper= " + (iSi != null ? iSi.getSymbol() : "null") + " " + workOn + " " +candleType + " " + StringUtils.leftPad(priceType.toString(), 15);
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
