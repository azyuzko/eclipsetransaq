package ru.eclipsetrader.transaq.core.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Strategy extends StrategyParamsType implements IProcessingContext, IStrategy {
	
	HashMap<TQSymbol, List<StrategyPosition>> signals = new HashMap<>();

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
	
	Lock signalLock = new ReentrantLock();

	int quantity = 1;
	
	public Strategy(IDataFeedContext dataFeedContext, StrategyParamsType params) {
		super(params);
		this.dataFeedContext = dataFeedContext;
		this.iBR = new Instrument(TQSymbol.BRU5, this, dataFeedContext);
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
	
	public void tick(Instrument i) {
		if (i.getSymbol().equals(TQSymbol.BRU5) /*|| i.getSymbol().equals(TQSymbol.RIU5)*/) {
			TQSymbol sSiU5 = TQSymbol.SiU5;
			
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
			
			Holder<Date[], double[]> valuesBr = iBR.getCandleStorage().getCandleList(_ct).values(_pt);
			Holder<Date[], double[]> valuesRI = iRI.getCandleStorage().getCandleList(_ct).values(_pt);
			Holder<Date[], double[]> valuesSi = iSi.getCandleStorage().getCandleList(_ct).values(_pt);
			
			StochasticFast sf = new StochasticFast(stochF_optInFastK_Period, stochF_optInFastD_Period, stochF_optInFastD_MAType);

			macdBr.evaluate(valuesBr.getSecond(), valuesBr.getFirst());
			macdRI.evaluate(valuesRI.getSecond(), valuesRI.getFirst());
			macdSi.evaluate(valuesSi.getSecond(), valuesSi.getFirst());

			double[] histBr = macdBr.getOutMACDHist();
			double[] histRI = macdRI.getOutMACDHist();
			double[] histSi = macdSi.getOutMACDHist();
			
			BuySell signalOpen = null;
			boolean needClose = false;

			if (	histBr.length > macdBr.getLookback() &&
					histRI.length > macdRI.getLookback() &&
					histSi.length > macdSi.getLookback() 
					) {

				int last_count = 10;
				StringBuilder sb = new StringBuilder();
				sb.append("\ndate      :" + Utils.printArray(last(valuesBr.getFirst(), last_count), "%10tR") + ", current = " + Utils.formatTime(getDateTime()) + " \n");					
				sb.append("BR price  :" + Utils.printArray(last(valuesBr.getSecond(), last_count), "%10.2f") + "\n");
				sb.append("BR hist   :" + Utils.printArray(last(macdBr.getOutMACDHist(), last_count), "%10.4f") + "\n");
				sb.append("RI price  :" + Utils.printArray(last(valuesRI.getSecond(), last_count), "%10.0f") + "\n");
				sb.append("RI hist   :" + Utils.printArray(last(macdRI.getOutMACDHist(), last_count), "%10.2f") + "\n");
				sb.append("Si price  :" + Utils.printArray(last(valuesSi.getSecond(), last_count), "%10.0f") + "\n");
				sb.append("Si hist   :" + Utils.printArray(last(macdSi.getOutMACDHist(), last_count), "%10.2f") + "\n");
				
				Double[] correlation = new Double[macdSi.getLookback()];
				for (int index = valuesBr.getFirst().length-macdSi.getLookback(), x = 0; index < valuesBr.getFirst().length; index++, x++) {
					correlation[x] = calcAllCorrelation(valuesBr.getSecond()[index], valuesRI.getSecond()[index], valuesSi.getSecond()[index]);
				}
				
				sb.append("correlatio:" + Utils.printArray(last(correlation, last_count), "%10.0f") + "\n");
				
				// 
				StrategyPosition currentPosition = currentOpenedPosition(sSiU5);
				double hist_br_const = 0.001;
				if (i.getSymbol().equals(TQSymbol.BRU5)) {
					if (!hasOpenedPosition(sSiU5)) {
						
						double brPrevLastDiff = valuesBr.getSecond()[valuesBr.getSecond().length-1] - valuesBr.getSecond()[valuesBr.getSecond().length-2];
						if (Math.abs(brPrevLastDiff) >= 0.04) {
							System.err.println("brPrevLastDiff >= 0.04");
							signalOpen = brPrevLastDiff < 0 ? BuySell.B : BuySell.S;
						} /*else 
						
						if  ( 
						Math.signum(histBr[histBr.length-1]) != Math.signum(histBr[histBr.length-2]) &&
							Math.abs(histBr[histBr.length-1]) > hist_br_const &&
							Math.abs(histBr[histBr.length-3]) > Math.abs(histBr[histBr.length-2])
							// correlation[correlation.length-1] >= 1339
								) {
							
							System.err.println("OPEN hist > " + hist_br_const);
							signalOpen = (Math.signum(histBr[histBr.length-1]) == -1) ? BuySell.B : BuySell.S;
							int count = 0;
							double sum = 0;
							for (int x = 0; x < correlation.length-2; x++) {
								sum += correlation[x];
								count++;
							}
							avg_corr = sum / count;
						}*/
					} else {
						
						System.err.println(String.format("%s %10.0f %10.2f %10.0f",
								Utils.formatTime(valuesSi.getFirst()[valuesSi.getFirst().length-1]),
								valuesSi.getSecond()[valuesSi.getSecond().length-1],
								macdSi.getOutMACDHist()[macdSi.getOutMACDHist().length-1],
								correlation[correlation.length-1]));
						
						/*							
						if (Math.signum(histBr[histBr.length-1]) != Math.signum(histBr[histBr.length-2]) && // !
							Math.abs(histBr[histBr.length-3]) > Math.abs(histBr[histBr.length-2]) &&
							Math.abs(histBr[histBr.length-2]) > Math.abs(histBr[histBr.length-1]) &&
							Math.abs(histBr[histBr.length-1]) > 0.001
							) {
							logger.info("******* Close by BR hist diff sign!");
							signal = openedPosition.getFirst().getOpposited();
						} else*/

						double planProfit = currentPosition.getPlanProfit(valuesSi.getSecond()[valuesSi.getSecond().length-1]);
						if (planProfit < - 10.0) {
							logger.info("******* Close STOP LOSS = " + planProfit);							
							needClose = true;
						}
						
						if (Math.signum(histSi[histSi.length-1]) == Math.signum(histSi[histSi.length-2]) &&
								Math.abs(histSi[histSi.length-3]) < Math.abs(histSi[histSi.length-2]) &&
								Math.abs(histSi[histSi.length-2]) > Math.abs(histSi[histSi.length-1]) &&
								( (currentPosition.getBuySell() == BuySell.B && valuesSi.getSecond()[valuesSi.getSecond().length-1] < valuesSi.getSecond()[valuesSi.getSecond().length-2])
								||	(currentPosition.getBuySell() == BuySell.S && valuesSi.getSecond()[valuesSi.getSecond().length-1] > valuesSi.getSecond()[valuesSi.getSecond().length-2]))
								) {
							logger.info("******* Close by BR hist Si!");
							needClose = true;
						}
						
						/*
						if (avg_corr != 0 && Math.abs(Math.round(correlation[correlation.length-1] - avg_corr)) < 2) {
							logger.info("******* Close correlation[correlation.length-1] = " + correlation[correlation.length-1]);							
							needClose = true;
							avg_corr = 0;
						}*/
					}
				}

				if (signalOpen == null && !needClose) {
					return;
				}
				
				if (!logger.isDebugEnabled()) {
					logger.info(sb.toString());
				}
				if (signalOpen != null) {
					currentPosition = openPosition(iSi, signalOpen);
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
				logger.info("Not enough history length = " + macdBr.getOutMACDHist().length + " for lookback " + macdBr.getLookback());
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
		dataFeedContext.OnStart(new Instrument[] { iSi, iBR, iRI });
		logger.debug("Started.");
	}
	
	private double calcAllCorrelation(double brPrice, double rtsPrice, double siPrice) {
		return Math.round( (siPrice + rtsPrice + brPrice * 1200) / 30 * 10) / 10 ;
	}

	private void printCorrelationTrace() {
		Map<Date, Candle> valuesSi = iSi.getCandleStorage().getCandleList(candleType).candleMap();
		Map<Date, Candle> valuesBr = iBR.getCandleStorage().getCandleList(candleType).candleMap();
		Map<Date, Candle> valuesRi = iRI.getCandleStorage().getCandleList(candleType).candleMap();
		
		System.out.println(" valuesSi.length = " + valuesSi.size());
		System.out.println(" valuesBr.length = " + valuesBr.size());
		System.out.println(" valuesRi.length = " + valuesRi.size());
		
		TreeMap<Double, Integer> mapBr = new TreeMap<>();
		TreeMap<Double, Integer> mapRi = new TreeMap<>();
		TreeMap<Double, Integer> mapAll = new TreeMap<>();
		
		for (Date dt : valuesBr.keySet()) {
			
			Candle cBr = valuesBr.get(dt);
			Candle cSi = valuesSi.get(dt);
			Candle cRi = valuesRi.get(dt);
			
			if (cSi == null) {
				System.err.println(Utils.formatTime(dt) + " not found Si");
				continue;
			}
			if (cRi == null) {
				System.err.println(Utils.formatTime(dt) + " not found Ri");
				continue;
			}
			
			double correlationBr = Math.round( cSi.getPriceValueByType(priceType) / cBr.getPriceValueByType(priceType) ) ;
			double correlationRi = Math.round( cSi.getPriceValueByType(priceType) / cRi.getPriceValueByType(priceType) * 1000) ;
			double correlationAll = calcAllCorrelation(cBr.getPriceValueByType(priceType),  cRi.getPriceValueByType(priceType), cSi.getPriceValueByType(priceType));
			Integer countBr = mapBr.get(correlationBr);
			Integer countRi = mapRi.get(correlationRi);
			Integer countAll = mapAll.get(correlationAll);
			countBr = countBr == null ? countBr = 1 : countBr + 1; 
			countRi = countRi == null ? countRi = 1 : countRi + 1; 
			countAll = countAll == null ? countAll = 1 : countAll + 1; 

			mapBr.put(correlationBr, countBr);
			mapRi.put(correlationRi, countRi);
			mapAll.put(correlationAll, countAll);			
		}

		System.out.println("\nBRENT:");
		for (Double key : mapBr.keySet()) {
			System.out.println(key + "  " + mapBr.get(key));
		}
		System.out.println("\nRTS:");
		for (Double key : mapRi.keySet()) {
			System.out.println(key + "  " + mapRi.get(key));
		}
		System.out.println("\nALL:");
		for (Double key : mapAll.keySet()) {
			System.out.println(key + "  " + mapAll.get(key));
		}	
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
	public void onTick(Instrument instrument, Tick tick) {
		
	}

	@Override
	public void onQuotesChange(Instrument instrument, QuoteGlass quoteGlass) {
		
	}

	@Override
	public void onCandleClose(Instrument instrument, CandleList candleList, Candle candle) {
			tick(instrument);			
	}

	@Override
	public void onCandleOpen(Instrument instrument, CandleList candleList, Candle candle) {
	
	}

	@Override
	public void onCandleChange(Instrument instrument, CandleList candleList, Candle candle) {

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
			for (TQSymbol symbol : getSymbols()) {
				QuantityCost position = positions.get(symbol);
				QuantityCost intial = account.getInitialPositions().get(symbol);
				Instrument i = getInstrument(symbol);
				if (position != null) {
					logger.debug("Closing position " + symbol + " = " + position);
					if (position.getQuantity() == intial.getQuantity()) {
						continue;
					} else if (position.getQuantity() > intial.getQuantity()) {
						i.sell(position.getQuantity() - intial.getQuantity());
					} else if (position.getQuantity() == 0) {
						i.buy(intial.getQuantity());
					}
					logger.debug("Closed " +symbol + "position ");
				} else if (intial != null) {
					i.buy(intial.getQuantity());
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
