package ru.eclipsetrader.transaq.core.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.account.QuantityCost;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.indicators.StochasticFast;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.tictactec.ta.lib.MAType;

public class Strategy implements IProcessingContext {
	
	HashMap<TQSymbol, List<StrategyPosition>> signals = new HashMap<>();

	Logger logger = LogManager.getLogger("Strategy");

	TQSymbol SiU5 = TQSymbol.SiU5;
	
	int fast = 9;
	int slow = 16;
	int signal = 9;
	
	int stochF_optInFastK_Period = 25;
	int stochF_optInFastD_Period = 4;
	MAType stochF_optInFastD_MAType = MAType.Trima;
	
	MACD macd;

	StochasticFast sf;
	
	IAccount account;
	Date currentDate = null;
	
	Lock signalLock = new ReentrantLock();

	int quantity = 1;
	
	public Strategy() {
		this.macd = new MACD(fast, slow, signal);
		this.sf = new StochasticFast(stochF_optInFastK_Period, stochF_optInFastD_Period, stochF_optInFastD_MAType);
	}
			
	public Strategy(IDataFeedContext dataFeedContext) {
		this();
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
	
	public void tick(CandleList candleList, Candle candle) {
		TQSymbol symbol = candleList.getSymbol();
		if (symbol.equals(SiU5)) {
			// 2 min wait after close position
			if (!hasOpenedPosition(SiU5)){
				StrategyPosition lastPosition = lastPosition(SiU5);
				if (lastPosition != null &&	DateUtils.addMinutes(lastPosition.getCloseDate(), 2).after(getDateTime())){
					logger.info(Utils.formatDate(getDateTime()) + " 2 min wait after close position");
					return;
				}
			}
			
			Date[] dates = candleList.stream().map(c -> c.getDate()).toArray(size -> new Date[size]);
		
			
			double[] valuesBr = candleList.streamPrice(PriceType.CLOSE).toArray();
			macd.evaluate(valuesBr, MAType.Ema);
			
			double[] hist = macd.getOutMACDHist();		
			
			sf.evaluate(candleList);
			double[] sfK = sf.getOutFastK();
			double[] sfD = sf.getOutFastD();
			

			BuySell signalOpen = null;
			boolean needClose = false;

			if (hist.length > macd.getLookback() &&
					sfK.length > sf.getLookback()) {

				int last_count = 10;
				StringBuilder sb = new StringBuilder();
				sb.append("\ndate      :" + Utils.printArray(last(dates, last_count), "%10tR") + ", current = " + Utils.formatTime(getDateTime()) + " \n");					
				sb.append("price  :" + Utils.printArray(last(valuesBr, last_count), "%10.2f") + "\n");
				sb.append("macd   :" + Utils.printArray(last(macd.getOutMACD(), last_count), "%10.4f") + "\n");
				sb.append("macdsig:" + Utils.printArray(last(macd.getOutMACDSignal(), last_count), "%10.4f") + "\n");
				sb.append("hist   :" + Utils.printArray(last(macd.getOutMACDHist(), last_count), "%10.4f") + "\n");
				sb.append("fast K :" + Utils.printArray(last(sfK, last_count), "%10.4f") + "\n");
				sb.append("fast D :" + Utils.printArray(last(sfD, last_count), "%10.4f") + "\n");
				sb.append("---\n");
				
				// 
				StrategyPosition currentPosition = currentOpenedPosition(SiU5);
				if (!hasOpenedPosition(SiU5)) {
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
					
					double planProfit = currentPosition.getPlanProfit(valuesBr[valuesBr.length-1]);
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
					currentPosition = openPosition(symbol, signalOpen);
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
	
	private StrategyPosition openPosition(TQSymbol symbol, BuySell buySell) {
		if (signalLock.tryLock()) {
			try {
				QuantityCost result = null;
				if (buySell == BuySell.B) {
					result = account.buy(symbol, quantity);
				} else {
					result = account.sell(symbol, quantity);
				}
				if (result.getQuantity() > 0) {
					StrategyPosition sp = new StrategyPosition(symbol, buySell);
					sp.setOpenDate(getDateTime());
					sp.setOpenCost(result.getCost());
					sp.setQuantity(result.getQuantity());
					List<StrategyPosition> list = signals.get(symbol);
					if (list == null) {
						list = new ArrayList<StrategyPosition>();
						signals.put(symbol, list);
					}
					list.add(sp);
					logger.warn(Utils.formatDate(getDateTime()) + " ****************************************** OPENED POSITION " +symbol+" signal " + buySell + " = " + quantity + " result = " + result) ;
					return sp;
				} else {
					logger.info("Position NOT OPENED on " +symbol + " " + buySell);
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				signalLock.unlock();
			}
		} else {
			logger.debug("Already locked for another buysell operation on " + symbol);
		}
		return null;
	}
	
	private boolean closePosition(StrategyPosition strategyPosition) {
		account.close(SiU5, 10.0);
		return true;
	}

	public void start(IAccount account) {
		logger.debug("Prepare to start...");
		this.account = account;
		logger.debug("Started.");
	}

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
	public void onQuotesChange(QuoteGlass quoteGlass) {
		
	}

	@Override
	public void onCandleClose(CandleList candleList, Candle candle) {
		tick(candleList, candle);			
	}

	@Override
	public void onQuotationsChange(TQSymbol symbol, Quotation quotation) {
		
	}



	
}
