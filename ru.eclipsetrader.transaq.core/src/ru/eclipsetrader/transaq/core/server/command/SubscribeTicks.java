package ru.eclipsetrader.transaq.core.server.command;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;
import ru.eclipsetrader.transaq.core.trades.TickSubscription;

public class SubscribeTicks {

	Set<TickSubscription> subscriptions  = new HashSet<>();	
	boolean filter = false;	

	public SubscribeTicks() {
		
	}

	public Set<TickSubscription> getSubscriptions() {
		return subscriptions;
	}



	public void addSubscription(TickSubscription tickSubscription){
		subscriptions.add(tickSubscription);
	}
	
	public void removeSubscription(ITQSymbol symbol) {
		Iterator<TickSubscription> it = subscriptions.iterator();
		while(it.hasNext()) {
			TickSubscription ts = it.next();
			if (ts.getSymbol().getBoard() == symbol.getBoard() && ts.getSymbol().getSeccode().equals(symbol.getSeccode())) {
				it.remove();
			}
		}
	}
	
	public String createCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"subscribe_ticks\">");
		for (TickSubscription ts : subscriptions) {
			sb.append("<security>");
			sb.append("<board>" + ts.getSymbol().getBoard() + "</board>");
			sb.append("<seccode>" + ts.getSymbol().getSeccode() + "</seccode>");
			sb.append("<tradeno>" + ts.getTradeno() + "</tradeno>");
			sb.append("</security>");
		}
		sb.append("<filter>" + false + "</filter>");
		sb.append("</command>");
		return sb.toString();
	}

	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}
	
}
