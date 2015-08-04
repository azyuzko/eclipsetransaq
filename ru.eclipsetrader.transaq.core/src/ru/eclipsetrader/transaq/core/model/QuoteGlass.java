package ru.eclipsetrader.transaq.core.model;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Стакан котировок
 * SortedMap<Цена, Кол-во> 
 * Для продажи кол-во со знаком минус
 * @author Zyuzko-AA
 *
 */
public class QuoteGlass {
	
	ConcurrentSkipListMap<Double, Integer> sellStack = new ConcurrentSkipListMap<Double, Integer>(); 
	ConcurrentSkipListMap<Double, Integer> buyStack = new ConcurrentSkipListMap<Double, Integer>(); 

	public ConcurrentSkipListMap<Double, Integer> getSellStack() {
		return sellStack;
	}

	public ConcurrentSkipListMap<Double, Integer> getBuyStack() {
		return buyStack;
	}

	/**
	 * Возвращает subMap стакана котировок для указанного количества лотов
	 * с минимальными ценами продажи.
	 * Если предложений в стакане не достаточно, возвращает столько, сколько есть.
	 * @param quantity - запрашиваемое кол-во лотов в стакане продажи
	 * @return
	 */
	public ConcurrentSkipListMap<Double, Integer> subSell(int quantity) {
		synchronized (this) {
			ConcurrentSkipListMap<Double, Integer> result = new ConcurrentSkipListMap<Double, Integer>();
			int remain_quantity = quantity;
			for (Double price : sellStack.keySet()) {
				int priceVolume = 0;
				try {
					priceVolume = sellStack.get(price);
				} catch (NullPointerException nex) {
					priceVolume = sellStack.get(price);
				}
				int extract_volume = Math.min(priceVolume, remain_quantity); // сколько можно вынуть из позиции стакана
				result.put(price, extract_volume);
				remain_quantity -= extract_volume;
				if (remain_quantity == 0) {
					break;
				}
				if (remain_quantity < 0) {
					throw new RuntimeException("Something wrong, remain < 0\n" + toString());
				}
			}
			return result;
		}
	}
	
	/**
	 * Возвращает subMap стакана котировок для указанного количества лотов
	 * с максимальными ценами покупки.
	 * Если предложений в стакане не достаточно, возвращает столько, сколько есть.
	 * @param quantity - запрашиваемое кол-во лотов в стакане покупки
	 * @return
	 */
	public ConcurrentSkipListMap<Double, Integer> subBuy(int quantity) {
		synchronized (this) {
			ConcurrentSkipListMap<Double, Integer> result = new ConcurrentSkipListMap<Double, Integer>(new Comparator<Double>() { // inverted order
				@Override
				public int compare(Double d1, Double d2) {
					return -Double.compare(d1, d2);
				}
			});
			int remain_quantity = quantity;
			for (Double price : buyStack.descendingKeySet()) {
				int priceVolume = buyStack.get(price);
				int extract_volume = Math.min(priceVolume, remain_quantity); // сколько можно вынуть из позиции стакана
				result.put(price, extract_volume);
				remain_quantity -= extract_volume;
				if (remain_quantity == 0) {
					break;
				}
				if (remain_quantity < 0) {
					throw new RuntimeException("Something wrong, remain < 0\n" + toString());
				}
			}
			return result;
		}
	}
	
	/**
	 * Обновляет стакан на основе котировок с сервера
	 * @param quotes
	 */
	public void update(List<Quote> quotes) {
		for (Quote quote : quotes) {
			update(quote);
		}
	}
	
	public void update(Quote quote) {
		synchronized (this) {
			if (quote.getSource() != null) {
				throw new RuntimeException("Field source not implemented yet in QuoteGlass");
			}
			
			double priceValue = quote.getPrice();
			
			if (priceValue == 0) {
				throw new RuntimeException();
			}
			
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
	
	public static void main(String[] args) {
		QuoteGlass qg = new QuoteGlass();
		qg.sellStack.put(50.0, 3);
		qg.sellStack.put(53.5, 2);
		qg.sellStack.put(55.7, 5);

		qg.buyStack.put(45.0, 3);
		qg.buyStack.put(42.5, 2);
		qg.buyStack.put(41.7, 5);
		
		ConcurrentSkipListMap<Double, Integer> res = qg.subSell(20);
		for (Double key : res.keySet()) {
			System.out.println("can sell " + res.get(key) + " lots by " + key);
		}
		
		res = qg.subBuy(20);
		for (Double key : res.keySet()) {
			System.out.println("can buy " + res.get(key) + " lots by " + key);
		}
	}
}
