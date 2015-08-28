package ru.eclipsetrader.transaq.core.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.indicators.StochasticFast;
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
import ru.eclipsetrader.transaq.core.trades.IFeedContext;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Strategy extends StrategyParamsType implements IProcessingContext, IStrategy {
	
	HashMap<TQSymbol, List<StrategyPosition>> signals = new HashMap<>();

	Logger logger = LogManager.getLogger("Strategy");

	public Instrument i;

	
	MACD macd;

	StochasticFast sf;
	
	IFeedContext feedContext;
	IAccount account;
	Date currentDate = null;
	
	Lock signalLock = new ReentrantLock();

	int quantity = 1;
	
	public Strategy(StrategyParamsType params) {
		super(params);
		this.macd = new MACD(fast, slow, signal);
		this.sf = new StochasticFast(stochF_optInFastK_Period, stochF_optInFastD_Period, stochF_optInFastD_MAType);
	}
			
	public Strategy(IDataFeedContext dataFeedContext, StrategyParamsType params) {
		this(params);
		this.feedContext = dataFeedContext;
		this.i = new Instrument(TQSymbol.SiU5, this, dataFeedContext);
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
	
	public StrategyPosition lastPosition(TQSymbol symbol) {
		if (signals.size() > 0 && signals.get(symbol) != null && signals.get(symbol).size() > 0) {
			return signals.get(symbol).get(signals.get(symbol).size()-1);
		}
		return null;
	}
	
	public StrategyPosition currentOpenedPosition(TQSymbol symbol) {
		StrategyPosition last = lastPosition(symbol);
		if (last != null && last.closeDate == null) {
			return last;
		}
		return null;
	}
	
	public boolean hasOpenedPosition(TQSymbol symbol) {
		return currentOpenedPosition(symbol) != null;
	}
	
	double avg_corr = 0;
	
	public void tick(TQSymbol symbol, CandleList candleList, Candle candle) {
		TQSymbol sSiU5 = TQSymbol.SiU5;
		if (symbol.equals(sSiU5)) {
			// 2 min wait after close position
			if (!hasOpenedPosition(sSiU5)){
				StrategyPosition lastPosition = lastPosition(sSiU5);
				if (lastPosition != null &&	DateUtils.addMinutes(lastPosition.getCloseDate(), 2).after(getDateTime())){
					logger.info(Utils.formatDate(getDateTime()) + " 2 min wait after close position");
					return;
				}
			}
			
			PriceType _pt = priceType;
			CandleType _ct = candleType;	
			
			Holder<Date[], double[]> valuesBr = i.getCandleStorage().getCandleList(_ct).values(_pt);
			macd.evaluate(valuesBr.getSecond(), valuesBr.getFirst());
			
			double[] hist = macd.getOutMACDHist();		
			
			sf.evaluate(i.getCandleStorage().getCandleList(candleType));
			double[] sfK = sf.getOutFastK();
			double[] sfD = sf.getOutFastD();
			

			BuySell signalOpen = null;
			boolean needClose = false;

			if (hist.length > macd.getLookback() &&
					sfK.length > sf.getLookback()) {

				int last_count = 10;
				StringBuilder sb = new StringBuilder();
				sb.append("\ndate      :" + Utils.printArray(last(valuesBr.getFirst(), last_count), "%10tR") + ", current = " + Utils.formatTime(getDateTime()) + " \n");					
				sb.append("price  :" + Utils.printArray(last(valuesBr.getSecond(), last_count), "%10.2f") + "\n");
				sb.append("macd   :" + Utils.printArray(last(macd.getOutMACD(), last_count), "%10.4f") + "\n");
				sb.append("macdsig:" + Utils.printArray(last(macd.getOutMACDSignal(), last_count), "%10.4f") + "\n");
				sb.append("hist   :" + Utils.printArray(last(macd.getOutMACDHist(), last_count), "%10.4f") + "\n");
				sb.append("fast K :" + Utils.printArray(last(sfK, last_count), "%10.4f") + "\n");
				sb.append("fast D :" + Utils.printArray(last(sfD, last_count), "%10.4f") + "\n");
				sb.append("---\n");
				
				// 
				StrategyPosition currentPosition = currentOpenedPosition(sSiU5);
				if (!hasOpenedPosition(sSiU5)) {
					if (   (sfD[sfD.length-1] > 80)
						|| (sfK[sfK.length-1] > 90)) {
						if (sfK[sfK.length-1] > sfK[sfK.length-2]) {
							signalOpen = BuySell.S;
						}
					} else 
						if ((sfD[sfD.length-1] < 20)
							|| (sfK[sfK.length-1] < 10)) {
							if (sfK[sfK.length-1] < sfK[sfK.length-2]) {
								signalOpen = BuySell.B;
							}
					}
					
				} else {
					
					double planProfit = currentPosition.getPlanProfit(valuesBr.getSecond()[valuesBr.getSecond().length-1]);
					if (planProfit < - 50.0) {
						logger.info("******* Close STOP LOSS = " + planProfit);							
						needClose = true;
					}
/*					
					if (Math.signum(histSi[histSi.length-1]) == Math.signum(histSi[histSi.length-2]) &&
							Math.abs(histSi[histSi.length-3]) < Math.abs(histSi[histSi.length-2]) &&
							Math.abs(histSi[histSi.length-2]) > Math.abs(histSi[histSi.length-1]) &&
							( (currentPosition.getBuySell() == BuySell.B && valuesBr.getSecond()[valuesBr.getSecond().length-1] < valuesBr.getSecond()[valuesBr.getSecond().length-2])
							||	(currentPosition.getBuySell() == BuySell.S && valuesBr.getSecond()[valuesBr.getSecond().length-1] > valuesBr.getSecond()[valuesBr.getSecond().length-2]))
							) {
						logger.info("******* Close by BR hist Si!");
						needClose = true;
					}*/

				}
				
				//if (!logger.isDebugEnabled()) {
				logger.info(sb.toString());
				//}

				if (signalOpen == null && !needClose) {
					return;
				}
				
				if (signalOpen != null) {
					currentPosition = openPosition(i, signalOpen);
					if (currentPosition == null) {
						logger.info("Position cannot be open");
					}
				}
				if (needClose) {
				boolean executed = closePosition(currentPosition); 
				if (executed) {
					currentPosition = null;
				} 				}
				logger.info("");			
				logger.info("");			
				logger.info("");			
			} else {
				logger.info(Utils.formatDate(getDateTime()) + " Not enough data hist.length = " + hist.length + "  macd.getLookback() = "
						+ macd.getLookback() + " sfK.length = " + sfK.length + " sf.getLookback() = " + sf.getLookback());
			}
		}
	}
	
	private Object[] last(Object[] values, int lastCount) {
		return ArrayUtils.subarray(values, values.length - lastCount, values.length);
	}

	private double[] last(double[] values, int lastCount) {
		return ArrayUtils.subarray(values, values.length - lastCount, values.length);
	}
	
	private StrategyPosition openPosition(Instrument i, BuySell buySell) {
		if (signalLock.tryLock()) {
			try {
				QuantityCost result = null;
				if (buySell == BuySell.B) {
					result = i.buy(quantity);
				} else {
					result = i.sell(quantity);
				}
				if (result.getQuantity() > 0) {
					StrategyPosition sp = new StrategyPosition(i.getSymbol(), buySell);
					sp.setOpenDate(getDateTime());
					sp.setOpenCost(result.getCost());
					sp.setQuantity(result.getQuantity());
					List<StrategyPosition> list = signals.get(i.getSymbol());
					if (list == null) {
						list = new ArrayList<StrategyPosition>();
						signals.put(i.getSymbol(), list);
					}
					list.add(sp);
					logger.warn(Utils.formatDate(getDateTime()) + " ****************************************** OPENED POSITION " +i.getSymbol() +" signal " + buySell + " = " + quantity + " result = " + result) ;
					return sp;
				} else {
					logger.info("Position NOT OPENED on " +i.getSymbol() + " " + buySell);
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
		return null;
	}
	
	private boolean closePosition(StrategyPosition strategyPosition) {
		if (signalLock.tryLock()) {
			try {
				QuantityCost result = null;
				if (strategyPosition.getBuySell() == BuySell.B) {
					result = getInstrument(strategyPosition.getSymbol()).sell(strategyPosition.getQuantity());
				} else {
					result = getInstrument(strategyPosition.getSymbol()).buy(strategyPosition.getQuantity());
				}
				
				if (result.getQuantity() > 0) {
					if (result.getQuantity() != strategyPosition.getQuantity()) {
						logger.warn("Position not fully closed!");
					}
					strategyPosition.setCloseDate(getDateTime());
					strategyPosition.setCloseCost(result.getCost());
					String log = "CLOSED " + strategyPosition;
					if (strategyPosition.getProfit() < 0) {
						// logger.error(log);
						System.err.println(log);
					} else {
						logger.warn(log) ;
					}
					return true;
				} else {
					logger.info("SIGNAL NOT EXECUTED on " +strategyPosition);
				}
				
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				signalLock.unlock();
			}
		} else {
			logger.debug("Already locked for another buysell operation on " + strategyPosition.getSymbol());
		}
		return false;
	}

	@Override
	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		feedContext.OnStart(new Instrument[] { i });
		logger.debug("Started.");
	}

	@Override
	public void stop() {
		logger.debug("Stopped");
		
		for (TQSymbol symbol : signals.keySet()) {
			List<StrategyPosition> list = signals.get(symbol);
			int goods = 0;
			double goodProfit = 0;
			int bads = 0;
			double badProfit = 0;
			for (StrategyPosition sp : list) {
				if (sp.getProfit() < 0) {
					bads += 1;
					badProfit += sp.getProfit();
				} else if (sp.getProfit() > 0) {
					goods += 1;
					goodProfit += sp.getProfit();
				}
				System.out.println(sp);
			}
			System.err.println(symbol + " GOODS: " + goods + " " + goodProfit);
			System.err.println(symbol + " BADS: " + bads + " " + badProfit);
		}
	}


	@Override
	public void onTick(TQSymbol symbol, Tick tick) {
		
	}

	@Override
	public void onQuotesChange(TQSymbol symbol, QuoteGlass quoteGlass) {
		
	}

	@Override
	public void onCandleClose(TQSymbol symbol, CandleList candleList, Candle candle) {
		tick(symbol, candleList, candle);			
	}

	@Override
	public void onQuotationsChange(TQSymbol symbol, Quotation quotation) {
		
	}

	@Override
	public CandleType[] getCandleTypes() {
		return new CandleType[] { candleType };
	}
	

	@Override
	public TQSymbol[] getSymbols() {
		return new TQSymbol[] {i.getSymbol() } ;
	}


	@Override
	public IProcessingContext getProcessingContext() {
		return this;
	}

	@Override
	public Instrument getInstrument(TQSymbol symbol) {
		if (symbol.equals(i.getSymbol())) {
			return i;
		} 
		return null;
	}

	@Override
	public void closePositions() {
		if (signalLock.tryLock()) {
			Map<TQSymbol, QuantityCost> positions = account.getPositions();
			logger.debug("Close positions size " + positions.size());
			for (TQSymbol symbol : getSymbols()) {
				QuantityCost position = positions.get(symbol);
				QuantityCost initial = account.getInitialPositions().get(symbol);
				if (initial == null) {
					initial = new QuantityCost(0, 0);
				}
				Instrument i = getInstrument(symbol);
				if (position != null) {
					logger.debug("Closing position " + symbol + " = " + position);
					if (position.getQuantity() == initial.getQuantity()) {
						continue;
					} else if (position.getQuantity() > initial.getQuantity()) {
						i.sell(position.getQuantity() - initial.getQuantity());
					} else if (position.getQuantity() == 0) {
						i.buy(initial.getQuantity());
					}
					logger.debug("Closed " +symbol + "position ");
				} else if (initial != null) {
					i.buy(initial.getQuantity());
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
