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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.TQSymbol;
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
		
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		Date fromDate = Utils.parseDate("03.08.2015 10:00:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:45:00.000");
		
		
		ExecutorService service = Executors.newFixedThreadPool(1);

		List<Future<Holder<Double, String>>> lr = new ArrayList<>();
		
		System.out.println("Data loaded..");
		int index = 0;
	
		
		DataFeeder dataFeeder = new DataFeeder(fromDate, toDate, 
				//TQSymbol.workingSymbolSet().toArray(new TQSymbol[0])); 
		 new TQSymbol[] {TQSymbol.SiU5});
		
		Strategy macd = new Strategy(dataFeeder);
		StrategyJob s = new StrategyJob(index++, macd, dataFeeder);
		Future<Holder<Double, String>> fut = service.submit(s);
		lr.add(fut);

		Logger logger = LogManager.getLogger("StrategyTest");
		logger.info("****** Jobs size = " + lr.size());

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
		
		logger.info("Completed!");
		logger.info("Sorted size = " + sorted.size());
		
		for (Double free : (new TreeMap<Double, List<String>>(sorted.tailMap(10000.0))).keySet()) {
			for (String name : sorted.get(free)) {
				System.out.println("Free: " + free + " = " +  name.substring(0, Math.min(2000, name.length()-1)));
			}
		}
		
		service.shutdown();
		
	}

}
