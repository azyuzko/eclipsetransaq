package ru.eclipsetrader.transaq.core.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.trades.IDataFeedContext;
import ru.eclipsetrader.transaq.core.trades.IDateTimeSupplier;
import ru.eclipsetrader.transaq.core.util.Utils;

public class SimpleAccount implements IAccount {
	
	Logger logger = LogManager.getLogger("SimpleAccount");

	double initialFree;
	double free;

	IDateTimeSupplier dateTimeSupplier;
	
	// holder = кол-во и размер ГО
	Map<TQSymbol, QuantityCost> positions = new HashMap<>();
	
	List<AccountOperation> operations = new ArrayList<AccountOperation>();
	
	public SimpleAccount(double free) {
		this(free, null);
	}
	
	public SimpleAccount(double free, IDateTimeSupplier dateTimeSupplier) {
		logger.debug("Create SimpleAccount with " + free + " cash");
		this.dateTimeSupplier = dateTimeSupplier;
		this.initialFree = free;
		this.free = free;
	}
	
	private AccountOperation createOperation(BuySell buySell, int quantity, double cost) {
		AccountOperation operation = new AccountOperation(buySell, currentDate(), quantity, cost);
		if (quantity > 0) {
			operations.add(operation);
		}
		return operation;
	}
	
	private Date currentDate() {
		if (dateTimeSupplier != null) {
			return dateTimeSupplier.getDateTime();
		} else {
			return new Date();
		}
	}
	
	public void reset() {
		this.free = initialFree;
		positions.clear();
		logger.info("Resetting account... Free = " + initialFree);
	}
	
	public double getFree() {
		return free;
	}
	
	public int getSimplePosition(TQSymbol symbol) {
		if (positions.containsKey(symbol)) {
			return positions.get(symbol).getQuantity();
		} else {
			return 0;
		}
	}

	@Override
	public QuantityCost buy(TQSymbol symbol, int quantity, double price) {
		if (logger.isDebugEnabled()) {
			logger.debug("buy " + symbol + " quantity = " + quantity + ", price = " + price);
		}
		double current_cost = quantity * price;
		if (free < current_cost) { 
			// посчитаем возможное количество
			quantity = (int) (free / price);
			current_cost = quantity * price;
		} 
		free -= current_cost;
		QuantityCost position = positions.get(symbol);
		if (position == null) {
			position = new QuantityCost(quantity, current_cost);
		} else {
			position = new QuantityCost(position.getQuantity() + quantity, position.getCost() + current_cost);
		}
		positions.put(symbol, position);
		return createOperation(BuySell.B, quantity, current_cost);
	}
	
	/**
	 * Покупка по ценам в стакане котировок
	 * @param symbol
	 * @param priceMap
	 */
	@Override
	public QuantityCost buy(TQSymbol symbol, int quantity, QuoteGlass quoteGlass) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("buy    " + symbol + " quantity = " + quantity + " from glass");
		}
		
		// определим, сколько готовы продать
		ConcurrentSkipListMap<Double, Integer> values = quoteGlass.subSell(quantity);
		double full_cost = 0;
		int available_quantity = 0;
		for (Double price : values.keySet()) {
			int price_quantity = values.get(price);
			if (free > ( full_cost + price * price_quantity) ) { // все еще хватает
				full_cost += price * price_quantity;
				available_quantity += price_quantity;
			} else {
				price_quantity = (int)((free - full_cost) / price);
				full_cost += price * price_quantity;
				available_quantity += price_quantity;
				break;
			}
		}
		
		free -= full_cost;
		QuantityCost position = positions.get(symbol);
		if (position == null) {
			position = new QuantityCost(available_quantity, full_cost);
		} else {
			position = new QuantityCost(position.getQuantity() + available_quantity, position.getCost() + full_cost);
		}
		positions.put(symbol, position);
		
		if (logger.isDebugEnabled()) {
			logger.debug("bought " + symbol + " quantity  = " + available_quantity + " on " + full_cost + ",   free = " + free);
		}
		
		return createOperation(BuySell.B, available_quantity, full_cost);
	}

	@Override
	public QuantityCost sell(TQSymbol symbol, int quantity, double price) {
		if (logger.isDebugEnabled()) {
			logger.debug("sell " + symbol + " quantity = " + quantity + ", price = " + price);
		}		
		QuantityCost position = positions.get(symbol);
		if (position == null) {
			return createOperation(BuySell.S, 0, 0);
		} else if (position.getQuantity() < quantity) { // есть в наличии
			quantity = position.getQuantity();
		}
		double current_cost = quantity * price; // цена продажи
		free += current_cost;
		position = new QuantityCost(position.getQuantity()-quantity, position.getCost()-current_cost); 
		if (position.getQuantity() == 0) { // если все позиции закрыты, снимем маржу
			positions.remove(symbol);
		} else {
			positions.put(symbol, position);
		}
		return createOperation(BuySell.S, quantity, current_cost);
	}
	
	@Override
	public QuantityCost sell(TQSymbol symbol, int quantity, QuoteGlass quoteGlass) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("sell " + symbol + " quantity = " + quantity + " from glass");
		}
		
		QuantityCost position = positions.get(symbol);
		if (position == null) {
			return createOperation(BuySell.S, 0, 0);
		}
		// определим, сколько готовы купить
		ConcurrentSkipListMap<Double, Integer> values = quoteGlass.subBuy(quantity);
		double full_cost = 0;
		int available_quantity = 0;
		for (Double price : values.keySet()) {
			if ( (available_quantity + values.get(price)) <= position.getQuantity()) {
				full_cost += price * values.get(price);
				available_quantity += values.get(price);
			} else {
				int rest_quantity = (position.getQuantity() - available_quantity); // снимем остаток
				full_cost += price * rest_quantity;
				available_quantity += rest_quantity;
			}
		}
		free += full_cost;
		position.setQuantity(position.getQuantity() - available_quantity);
		if (position.getQuantity() == 0) {
			positions.remove(symbol);
		} 
		
		if (logger.isDebugEnabled()) {
			logger.debug("sold " + symbol + " quantity = " + available_quantity + " on " + full_cost + ",   free = " + free);
		}
		
		return createOperation(BuySell.S, available_quantity, full_cost);
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Free: " + free);
		for (TQSymbol symbol : positions.keySet()) {
			QuantityCost position = positions.get(symbol);
			sb.append("\n" + symbol + ": " + position.getQuantity() + " - " + position.getCost());
		}
		sb.append(" Operations: " + operations.size());
		for (AccountOperation ao : operations) {
			sb.append(" {" + Utils.formatTime(ao.getTime()) + "_" + ao.getBuySell() + "_" + ao.getQuantity() + "_" + ao.getCost() + "}");
		}
		return sb.toString();
	}
	
	public String operations() {
		StringBuilder sb = new StringBuilder();
		for (AccountOperation operation : operations) {
			sb.append(Utils.formatDate(operation.getTime()) + " - " + operation.buySell + " quantity = " + operation.getQuantity() + ", cost = " + operation.getCost() + "\n");
		}
		return sb.toString();
	}
	

	@Override
	public Map<TQSymbol, QuantityCost> getPositions() {
		return positions;
	}


	@Override
	public QuantityCost close(TQSymbol symbol, double price) {
		QuantityCost postition = positions.get(symbol);
		if (postition == null || postition.getQuantity() == 0) {
			return createOperation(BuySell.S, 0, 0);
		}
		return sell(symbol, postition.getQuantity(), price);
	}
	
	public static void main(String[] args) {
		SimpleAccount sa = new SimpleAccount(1000);
		TQSymbol s1 = new TQSymbol(BoardType.FUT, "SiU5");
		TQSymbol s2 = new TQSymbol(BoardType.FUT, "BRQ5");
		System.out.println("buy = " + sa.buy(s1, 15, 100.0));
		System.out.println("sell = " + sa.sell(s1, 10, 120));
		System.out.println("sell = " + sa.sell(s1, 8, 100));
		System.out.println(sa);		
		
		System.out.println("----");
		sa = new SimpleAccount(1000);
		QuoteGlass qg = new QuoteGlass();
		qg.getSellStack().put(60.0, 2);
		qg.getSellStack().put(55.0, 2);
		qg.getSellStack().put(50.0, 1);
		
		qg.getBuyStack().put(45.0, 1);
		qg.getBuyStack().put(40.0, 2);
		qg.getBuyStack().put(50.0, 2);
		
		System.out.println(sa);		
		System.out.println("buy = " + sa.buy(s1, 5, qg));
		
		System.out.println(sa);		
		System.out.println("sell = " + sa.sell(s1, 4, qg));
		System.out.println(sa.initialFree);				
		System.out.println(sa.free);
	}


}
