package ru.eclipsetrader.transaq.core.trades;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Holder;

public class DataFeeder {
	
	LinkedList<TickTrade> feedQueue;

	public DataFeeder(List<TickTrade> feed) {
		this.feedQueue = new LinkedList<TickTrade>(feed);
	}
	
	public static List<TickTrade> getTradeList(TQSymbol symbol, Date dateFrom, Date dateTo) {
		@SuppressWarnings("unchecked")
		List<TickTrade> resultList = DataManager.executeQuery("select t from TickTrade t where t.symbol.board = :board and t.symbol.seccode = :seccode and t.time between :from and :to order by t.time",
				new Holder<String, Object>("board", symbol.getBoard()), new Holder<String, Object>("seccode", symbol.getSeccode()),
				new Holder<String, Object>("from", dateFrom), new Holder<String, Object>("to", dateTo)
				);
		return resultList;
	}
	
	public TickTrade getNext() {
		return feedQueue.poll();
	}

	public void feed(Instrument i, IProcessingContext signalEventHandler) {
		TickTrade tickTrade;
		//System.out.println("start feed");
		while ((tickTrade = getNext()) != null) {
			i.processTrade(tickTrade);
			signalEventHandler.completeTrade(tickTrade, i);
		}
		signalEventHandler.complete(i);
		//System.out.println("end feed");
	}
	
	public static void main(String[] args) {
				
	}

	
}
