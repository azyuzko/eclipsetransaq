package ru.eclipsetrader.transaq.core.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TickList {
	
	static int MAX_SIZE = 200;
	
	LinkedList<Tick> tickQueue;
	
	public TickList() {
		this.tickQueue = new LinkedList<>();
	}

	public synchronized void add(Tick tick) {
		tickQueue.add(tick);
		while (tickQueue.size() > MAX_SIZE) {
			tickQueue.remove(0);
		}
	}

	public synchronized void add(List<Tick> tickList) {
		tickQueue.addAll(tickList);
		while (tickQueue.size() > MAX_SIZE) {
			tickQueue.remove(0);
		}
	}
	
	public Tick last() {
		return tickQueue.getLast();
	}
	
	public synchronized Stream<Tick> stream(boolean desc) {
		if (desc) {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(tickQueue.descendingIterator(),
	                    Spliterator.ORDERED), false);
		} else {
			return tickQueue.stream().sorted((c1,c2) -> c1.getTime().compareTo(c2.getTime()));
		}
	}

	public Stream<Tick> stream() {
		return stream(false);
	}


	public static void main(String[] args) {
		TQSymbol[] symbols = new TQSymbol[] {TQSymbol.BRV5};
		
		Date fromDate = Utils.parseDate("03.08.2015 09:30:00.000");
		Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
		
		List<TickTrade> ttList = DataManager.getTickList(fromDate, toDate, symbols);
		
		List<Tick> res = ttList.stream().filter(t -> t.getBuysell() == BuySell.S).collect(Collectors.toCollection(ArrayList<Tick>::new));
		
		TickList tl = new TickList();
		tl.add(res);
		
		// tl.stream().forEach(s -> { System.out.println(s + "  " + tl.speed(s.getTime(), 60)); });
	
		
	}

}
