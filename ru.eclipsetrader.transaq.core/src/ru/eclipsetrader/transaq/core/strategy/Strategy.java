package ru.eclipsetrader.transaq.core.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleColor;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Strategy extends StrategyParamsType implements IProcessingContext, IStrategy {
	
	HashMap<TQSymbol, List<Signal>> signals = new HashMap<>();

	Logger logger = LogManager.getLogger("Strategy");

	public Instrument iBR;
	public Instrument iRI;
	public Instrument iSi;

	MACD macdBr = new MACD(fast, slow, signal);
	MACD macdRI = new MACD(fast, slow, signal);
	MACD macdSi = new MACD(fast, slow, signal);

	IDataFeedContext dataFeedContext;
	IAccount account;
	Date currentDate = null;
	
	public Strategy(IDataFeedContext dataFeedContext, StrategyParamsType params) {
		super(params);
		this.dataFeedContext = dataFeedContext;
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

	boolean opened = false;
	BuySell bs;
	
	Date lastPosition = null;
	
	public void tick(Instrument i) {
		if (i.getSymbol().equals(TQSymbol.BRQ5) /*|| i.getSymbol().equals(TQSymbol.RIU5)*/) {
			
			PriceType _pt = priceType;
			CandleType _ct = candleType;	
			
			Holder<Date[], double[]> valuesBr = iBR.getCandleStorage().getCandleList(_ct).values(_pt);
			Holder<Date[], double[]> valuesRI = iRI.getCandleStorage().getCandleList(_ct).values(_pt);
			Holder<Date[], double[]> valuesSi = iSi.getCandleStorage().getCandleList(_ct).values(_pt);

			macdBr.evaluate(valuesBr.getSecond(), valuesBr.getFirst());
			macdRI.evaluate(valuesRI.getSecond(), valuesRI.getFirst());
			macdSi.evaluate(valuesSi.getSecond(), valuesSi.getFirst());

			double[] histBr = macdBr.getOutMACDHist();
			double[] histRI = macdRI.getOutMACDHist();
			double[] histSi = macdSi.getOutMACDHist();
			
			if (	histBr.length > macdBr.getLookback() &&
					histRI.length > macdRI.getLookback() &&
					histSi.length > macdSi.getLookback() 
					) {

				StringBuilder sb = new StringBuilder();
				sb.append("\ndate      :" + Utils.printArray(last(valuesBr.getFirst(), 20), "%10tR") + ", current = " + Utils.formatTime(getDateTime()) + " \n");					
				sb.append("BR price  :" + Utils.printArray(last(valuesBr.getSecond(), 20), "%10.2f") + "\n");
				sb.append("BR hist  :" + Utils.printArray(last(macdBr.getOutMACDHist(), 20), "%10.4f") + "\n");
				sb.append("RI price  :" + Utils.printArray(last(valuesRI.getSecond(), 20), "%10.0f") + "\n");
				sb.append("RI hist  :" + Utils.printArray(last(macdRI.getOutMACDHist(), 20), "%10.2f") + "\n");
				sb.append("Si price  :" + Utils.printArray(last(valuesSi.getSecond(), 20), "%10.0f") + "\n");
				sb.append("Si hist  :" + Utils.printArray(last(macdSi.getOutMACDHist(), 20), "%10.2f") + "\n");
				

				if (lastPosition != null && DateUtils.addMinutes(lastPosition, 2).after(getDateTime())) {
					logger.debug("Not passed 2 minutes from last position = " + Utils.formatTime(lastPosition) + "  current = " + Utils.formatTime(getDateTime()));
					return;
				}
				
				logger.debug(sb.toString());

				bs = null;
				// 
				double hist_br_const = 0.007;
				if (i.getSymbol().equals(TQSymbol.BRQ5)) {
					if (!opened) {
						if (Math.signum(histBr[histBr.length-1]) == Math.signum(histBr[histBr.length-2]) &&
							Math.abs(histBr[histBr.length-1]) > hist_br_const &&
							Math.abs(histBr[histBr.length-2]) < Math.abs(histBr[histBr.length-1]) &&
							Math.abs(histBr[histBr.length-3]) < Math.abs(histBr[histBr.length-2])
								) {
							logger.debug("hist > " + hist_br_const);
							bs = (Math.signum(histBr[histBr.length-1]) == -1) ? BuySell.B : BuySell.S;
							opened = true;
						}
					} else {
						
						if (Math.signum(histBr[histBr.length-1]) == Math.signum(histBr[histBr.length-2]) &&
								Math.abs(histBr[histBr.length-3]) > Math.abs(histBr[histBr.length-2]) &&
								Math.abs(histBr[histBr.length-2]) > Math.abs(histBr[histBr.length-1]) &&
								(Math.abs(histBr[histBr.length-2]) - Math.abs(histBr[histBr.length-1])) < 0.001
								) {
							bs = (Math.signum(histBr[histBr.length-1]) == -1) ? BuySell.B : BuySell.S;
							opened = false;
						} else
							
						if (Math.signum(histBr[histBr.length-1]) != Math.signum(histBr[histBr.length-2])) {
							bs = (Math.signum(histBr[histBr.length-1]) == -1) ? BuySell.B : BuySell.S;
							opened = false;
						} else 							
						
						if (Math.signum(histSi[histSi.length-1]) == Math.signum(histSi[histSi.length-2]) &&
								Math.abs(histSi[histSi.length-3]) < Math.abs(histSi[histSi.length-2]) &&
								Math.abs(histSi[histSi.length-2]) > Math.abs(histSi[histSi.length-1])) {
							bs = (Math.signum(histSi[histSi.length-1]) == -1) ? BuySell.B : BuySell.S;
							opened = false;
						}
					}
				}
				
				if (bs == null && i.getSymbol().equals(TQSymbol.RIU5)) {
					double[] hist = macdRI.getOutMACDHist(); 
					if (Math.signum(hist[hist.length-1]) != Math.signum(hist[hist.length-2])
							&& (Math.abs(hist[hist.length-3]) > Math.abs(hist[hist.length-2]))
							&& (Math.abs(hist[hist.length-2]) > Math.abs(hist[hist.length-1]))
							//&& (Math.abs(hist[hist.length-1]) > 0.001)
							) {
						logger.debug("simple ");
						bs = (Math.signum(hist[hist.length-1]) == -1) ? BuySell.B : BuySell.S;
					}
				}
				
				if (bs == null) {
					return;
				}
				
				double priceSi = valuesSi.getSecond()[valuesSi.getSecond().length-1];
				logger.info(Utils.formatTime(getDateTime()) + " DETECTED SIGNAL FROM " + i.getSymbol().getSeccode() + " ********************************************************** " + bs + " = " + priceSi);					
				if (!logger.isDebugEnabled()) {
					logger.info(sb.toString());
				}
				if (createSignal(iSi, bs, priceSi)) {
					lastPosition = getDateTime();
				} else {
					logger.info("&&& NOT EXECUTED!");

				}
				logger.info("");			
			} else {
				logger.info("Not enough history length = " + macdBr.getOutMACDHist().length + " for lookback " + macdBr.getLookback());
			}
		}
	}
	
	private BuySell checkCandleSignal(CandleList candleList) {
		List<Candle> list = candleList.candleList();
		int size = list.size();
		if (size <= 2) {
			return null;
		}
		Candle c1 = list.get(size-1);
		Candle c2 = list.get(size-2);
		Candle c3 = list.get(size-3);
		BuySell bs = null;
		if (c1.getCandleColor() == CandleColor.BLACK
				&& c2.getCandleColor() == CandleColor.BLACK
				&& c3.getCandleColor() == CandleColor.BLACK
				&& c1.getClose() < c2.getClose()
				&& c2.getClose() < c3.getClose()
				&& ((c1.getClose() - c2.getClose()) < (c2.getClose() - c3.getClose())/3) 
				) {
			bs = BuySell.B;
		}
		if (c1.getCandleColor() == CandleColor.WHITE
				&& c2.getCandleColor() == CandleColor.WHITE
				&& c3.getCandleColor() == CandleColor.WHITE
				&& c1.getClose() > c2.getClose()
				&& c2.getClose() > c3.getClose()
				&& ((c1.getClose() - c2.getClose()) < (c2.getClose() - c3.getClose())/3)
				) {
			bs = BuySell.S;
		}
		if (bs != null) {
			Holder<Date[], double[]> values = candleList.values(PriceType.CLOSE, 10);
			StringBuilder sb = new StringBuilder();
			sb.append("Signal " + bs + " " + Utils.formatDate(getDateTime()) + " Price : " + values.getSecond()[values.getSecond().length-1] + "\n");
			sb.append("date      :" + Utils.printArray(last(values.getFirst(), 10), "%7tR") + "\n");					
			sb.append("Si price  :" + Utils.printArray(last(values.getSecond(), 10), "%7.0f") + "\n");
			logger.info(sb.toString());
		}
		return bs;
	}
	
	public void tickCandle(Instrument i, CandleList candleList, Candle candle) {
		if (i.getSymbol().equals(TQSymbol.SiU5)) {
			if (signalLock.tryLock()) {
				try {
					BuySell bs = checkCandleSignal(candleList);
					if (bs != null) {
						int	result = 0;
						if (bs == BuySell.S) {
							result = i.sell(quantity).getQuantity();
						} else {
							result = i.buy(quantity).getQuantity();
						}
						if (result > 0) {
							Holder<Date[], double[]> data = candleList.values(PriceType.CLOSE);
							double[] values = data.getSecond();
							Date[] dates = data.getFirst();
							
							Signal s = new Signal(i.getSymbol(), getDateTime(), bs, values[values.length-1]);
							List<Signal> list = signals.get(i.getSymbol());
							if (list == null) {
								list = new ArrayList<Signal>();
								signals.put(i.getSymbol(), list);
							}
							list.add(s);	
							StringBuilder sb = new StringBuilder();
							sb.append("Signal EXECUTED " + bs + " " + Utils.formatDate(currentDate) + " Price : " + s.getPrice() + "\n");
							logger.info(sb.toString());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					signalLock.unlock();
				}
			} else {
				logger.debug("Already locked for another buysell operation on " + i.getSymbol());
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

	private boolean createSignal(Instrument i, BuySell buySell, double priceSi) {
		if (signalLock.tryLock()) {
			try {
				logger.info("try execute signal on " +i.getSymbol() + " " + buySell + " Si = " + priceSi);
				
				int result = 0;
				if (buySell == BuySell.B) {
					result = i.buy(quantity).getQuantity();
				} else {
					result = i.sell(quantity).getQuantity();
				}
				if (result > 0) {
					Signal s = new Signal(i.getSymbol(), getDateTime(), buySell, priceSi);
					List<Signal> list = signals.get(i.getSymbol());
					if (list == null) {
						list = new ArrayList<Signal>();
						signals.put(i.getSymbol(), list);
					}
					list.add(s);
					logger.warn(Utils.formatDate(getDateTime()) + " ****************************************** EXECUTED " +i.getSymbol() +" signal " + buySell + " = " + quantity + " result = " + result + ", price ???? = " + priceSi) ;
					return true;
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				signalLock.unlock();
			}
		} else {
			logger.debug("Already locked for another buysell operation on " + i.getSymbol());
		}
		return false;
	}
		

	@Override
	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		dataFeedContext.OnStart(new Instrument[] { iSi, iBR, iRI });
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
		//tickCandle(instrument, candleList, candle);
		tick(instrument);
	}

	@Override
	public void onCandleOpen(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleOpen: " + instrument.getSymbol() + " " + candle.toString());
		
	}

	@Override
	public void onCandleChange(Instrument instrument, CandleList candleList, Candle candle) {
//		System.out.println("onCandleChange: " + instrument.getSymbol() + " " + candle.toString());
		/*if (workOn == StrategyWorkOn.CandleChange) {
			tick(instrument);
		}*/
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
		return "STR fast= " + slow + ", slow= " + fast + ", signal= " + signal + " iWatch= " + (iBR != null ? iBR.getSymbol() : "null") + " iOper= " + (iSi != null ? iSi.getSymbol() : "null") + " " + workOn + " " +candleType + " " + StringUtils.leftPad(priceType.toString(), 15);
	}

	@Override
	public void closePositions() {
		if (signalLock.tryLock()) {
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
		} else {
			logger.debug("Cannot close cause already locked");
		}
	}

	@Override
	public IAccount getAccount() {
		return account;
	}


	
}
