package ru.eclipsetrader.transaq.core.model;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.interfaces.IQuotesProcessingContext;
import ru.eclipsetrader.transaq.core.util.Utils;
/**
 * Стакан котировок
 * SortedMap<Цена, Кол-во> 
 * Для продажи кол-во со знаком минус
 * @author Zyuzko-AA
 *
 */
public class QuoteGlass {
	
	Logger logger = LogManager.getLogger("QuoteGlass");
	
	TQSymbol symbol;
	
	ConcurrentSkipListMap<Double, Integer> sellStack = new ConcurrentSkipListMap<Double, Integer>(); 
	ConcurrentSkipListMap<Double, Integer> buyStack = new ConcurrentSkipListMap<Double, Integer>();
	
	IQuotesProcessingContext quotesProcessingContext;
	
	ListObserver<Quote> iQuotesObserver = (List<Quote> list) -> {
		if (logger.isDebugEnabled()) {
			logger.debug("iQuotesObserver " + list.get(0).getSeccode() + "  size = " + list.size() + " " + Utils.formatTime(list.get(0).getTime()) + " -- " + Utils.formatTime(list.get(list.size()-1).getTime()) );
		}
		updateQuotes(list);
	};
	
	public ListObserver<Quote> getQuotesObserver() {
		return iQuotesObserver;
	}
	
	public QuoteGlass(TQSymbol symbol) {
		logger.debug("QuoteGlass for " + symbol + "created");
		this.symbol = symbol;
	}
	
	public IQuotesProcessingContext getQuotesProcessingContext() {
		return quotesProcessingContext;
	}

	public void setQuotesProcessingContext(
			IQuotesProcessingContext quotesProcessingContext) {
		this.quotesProcessingContext = quotesProcessingContext;
	}

	public TQSymbol getSymbol() {
		return symbol;
	}

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
			if (logger.isDebugEnabled()) {
				logger.debug("subSell = " + quantity);
			}
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
			if (logger.isDebugEnabled()) {
				logger.debug("subBuy = " + quantity);
			}

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
	public void updateQuotes(List<Quote> quotes) {
		for (Quote quote : quotes) {
			updateQuotes(quote);
		}
		if (quotesProcessingContext != null) {
			quotesProcessingContext.onQuotesChange(this);
		}
	}
	
	public void updateQuotes(Quote quote) {
		synchronized (this) {
			if (quote.getSource() != null) {
				throw new RuntimeException("Field source not implemented yet in QuoteGlass");
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Update glass = " + quote.getSeccode() + " B " + quote.getBuy() + " S " + quote.getSell() + " Price " + quote.getPrice()) ;
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
		return toString(2);
	}
	
	public String toString(int count) {
		StringBuilder sb = new StringBuilder();
		sellStack.descendingKeySet().stream().limit(count).forEach(price ->	{ sb.append(price); sb.append(" "); sb.append(sellStack.get(price)); sb.append("\n");});
		sb.append("------\n");
		buyStack.descendingKeySet().stream().limit(count).forEach(price -> {sb.append(price); sb.append(" "); sb.append(buyStack.get(price)); sb.append("\n");});
		return sb.toString();
	}
	
	public static void main(String[] args) throws InterruptedException {
		QuoteGlass qg = new QuoteGlass(TQSymbol.BRU5);
		qg.sellStack.put(53.5, 2);
		qg.sellStack.put(50.0, 3);
		qg.sellStack.put(55.7, 5);

		qg.buyStack.put(42.5, 2);
		qg.buyStack.put(45.0, 3);
		qg.buyStack.put(41.7, 5);
		
		System.out.println(qg); 
		
	}
}
