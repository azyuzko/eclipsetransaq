package ru.eclipsetrader.transaq.core.model;

import java.util.TreeMap;
/**
 * Стакан котировок
 * SortedMap<Цена, Кол-во> 
 * Для продажи кол-во со знаком минус
 * @author Zyuzko-AA
 *
 */
public class QuoteGlass {
	
	TreeMap<Double, Integer> sellStack = new TreeMap<Double, Integer>(); 
	TreeMap<Double, Integer> buyStack = new TreeMap<Double, Integer>(); 
	
	public void update(Quote quote) {
		if (quote.getSource() != null) {
			throw new RuntimeException("Field source not implemented yet in QuoteGlass");
		}
		
		double priceValue = quote.getPrice();
		int buy = quote.getBuy();
		int sell = quote.getSell();
		if (buy == -1) {
			buyStack.remove(priceValue);
		} else if (buy > 0) {
			buyStack.put(priceValue, buy);
		}
		if (sell == -1) {
			sellStack.remove(priceValue);
		} else if (sell > 0) {
			sellStack.put(priceValue, sell);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Double price : sellStack.descendingKeySet()) {
			sb.append(price); sb.append(" "); sb.append(sellStack.get(price)); sb.append("\n");
		}
		sb.append("------\n");
		for (Double price : buyStack.descendingKeySet()) {
			sb.append(price); sb.append(" "); sb.append(buyStack.get(price)); sb.append("\n");
		}
		return sb.toString();
	}
	
}
