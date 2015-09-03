package ru.eclipsetrader.transaq.core.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.google.common.collect.Iterators;

public class TickSet {
	
	CircularFifoQueue<Tick> tickQueue;
	
	public TickSet(int size) {
		this.tickQueue = new CircularFifoQueue<>(size);
	}
	
	public TickSet() {
		this.tickQueue = new CircularFifoQueue<>(100);
	}

	public void add(Tick tick) {
		synchronized (tickQueue) {
			tickQueue.add(tick);	
		}
	}

	public void add(List<Tick> tickList) {
		synchronized (tickQueue) {
			tickQueue.addAll(tickList);	
		}
	}
	
	public void analyze() {
		synchronized (tickQueue) {
			int buyVolume = 0;
			int sellVolume = 0;
			for (Tick t : tickQueue) {
				if (t.getBuysell() == BuySell.B) {
					buyVolume += t.getQuantity();
				}
				if (t.getBuysell() == BuySell.S) {
					sellVolume += t.getQuantity();
				}
			}
			System.out.println("Volume buy: " + buyVolume + " sell: " + sellVolume);
		}
	}
	
	public TickSet last(int count) {
		TickSet ts = new TickSet(count);
		for (Tick t : tickQueue) {
			ts.add(t);
		}
		return ts;
	}
	
	public TickSet lastBuy(int count) {
		
		Iterators.transform(tickQueue.iterator(), null);
		TickSet ts = new TickSet(count);
		for (Tick t : tickQueue) {
			if (t.getBuysell() == BuySell.B) {
				ts.add(t);
			}
		}
		return ts;
	}
	
	public List<Tick> lastSell(int count) {
		List<Tick> result = tickQueue.stream().filter(t -> (t.getBuysell() == BuySell.S)).limit(count).collect(Collectors.toList());
		return result;
	}
	
	interface HelloWorld {
		String hello(String name);
	}


	public static void main(String[] args) {
		TQSymbol[] symbols = new TQSymbol[] {TQSymbol.BRU5};
		
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
		
		List<TickTrade> ttList = DataManager.getTickList(fromDate, toDate, symbols);
		
		List<Tick> res = ttList.stream().filter(t -> t.getBuysell() == BuySell.S).collect(Collectors.toCollection(ArrayList<Tick>::new));
		
		
		/*LinkedList<Integer> li = new LinkedList<Integer>();
		for (int i = 0; i < 1000; i++) {
			li.add(i);
		}
		li.stream().sorted((a,b) -> a.compareTo(b)).limit(10).filter(i -> (i > 100) ).forEach(x -> System.out.println(x));
		*/
		
		 System.out.println(res.size());
		
	}

}
