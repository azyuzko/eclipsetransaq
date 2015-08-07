package ru.eclipsetrader.transaq.core.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.strategy.Strategy.WorkOn;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
public class StrategyTest {
	
	@Id
	@SequenceGenerator(name="SEQ_GEN", sequenceName="SEQ_STRATEGYTEST", allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_GEN")
	Integer id;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date dateFrom;
	@Temporal(TemporalType.TIMESTAMP)
	Date dateTo;

	@Temporal(TemporalType.TIMESTAMP)
	Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	Date completeDate;

	@Transient
	DataFeeder dataFeeder;
	
	public StrategyTest() {
		
	}
	
	public StrategyTest(Date fromDate, Date toDate) {
		this.dateFrom = fromDate;
		this.dateTo = toDate;
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate, 
		//		TQSymbol.workingSymbolSet().toArray(new TQSymbol[0])); 
		 new TQSymbol[] {TQSymbol.RTSI, TQSymbol.SiU5});
		
		ExecutorService service = Executors.newFixedThreadPool(10);

		List<Future<Holder<Double, String>>> lr = new ArrayList<>();
		
		List<Holder<TQSymbol, TQSymbol>> symbolList = new ArrayList<Holder<TQSymbol,TQSymbol>>();
		
		symbolList.add(new Holder<TQSymbol, TQSymbol>(TQSymbol.RTSI, TQSymbol.SiU5));

		int index = 0;
		for (int fast = 6; fast <= 6; fast++) {
			for (int slow = 12; slow <= 12; slow++) {
				for (int signal = 9; signal <= 9; signal++) {
					for (Holder<TQSymbol, TQSymbol> symbols : symbolList) {
						for (WorkOn workOn : WorkOn.values()) {
							for (CandleType candleType : new CandleType[] {
									CandleType.CANDLE_15S
									} ) {
								for (PriceType priceType : new PriceType[] {PriceType.CLOSE}) {
									// create params
									StrategyParamsType sp = new StrategyParamsType();
									sp.setFast(fast);
									sp.setSlow(slow);
									sp.setSignal(signal);
									sp.setPriceType(priceType);
									sp.setWorkOn(workOn);
									sp.setWatchSymbol(symbols.getFirst());
									sp.setOperSymbol(symbols.getSecond());
									sp.setCandleType(candleType);
									
									Strategy macd = new Strategy(dataFeeder, sp);
									StrategyJob s = new StrategyJob(index++, macd, dataFeeder);
									Future<Holder<Double, String>> fut = service.submit(s);
									lr.add(fut);
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println("****** Jobs size = " + lr.size());

		TreeMap<Double, List<String>> sorted = new TreeMap<>();
		
		for (Future<Holder<Double, String>> f : lr) {
			Holder<Double, String> result = f.get();
			List<String> list = sorted.get(result.getFirst());
			if (list == null) {
				list = new ArrayList<String>();
				sorted.put(result.getFirst(), list);
			}
			list.add(result.getSecond());
		}
		
		System.out.println("sorted size = " + sorted.size());
		
		for (Double free : (new TreeMap<Double, List<String>>(sorted.tailMap(10000.0))).descendingKeySet()) {
			for (String name : sorted.get(free)) {
				System.out.println("Free: " + free + " = " +  name.substring(0, Math.min(2000, name.length()-1)));
			}
		}
		
		service.shutdown();
		
	}

}
