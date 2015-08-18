package ru.eclipsetrader.transaq.core.account;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.exception.UnimplementedException;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.IFortsQuotesSupplier;
import ru.eclipsetrader.transaq.core.model.BaseFortsContract;
import ru.eclipsetrader.transaq.core.model.BaseFortsMoney;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.QuoteGlass;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;

public class FortsAccountSimulator implements IAccount {

	private static final double COMISSION_RATE = 0.0001; // % от суммы сделки
	
	TreeMap<Date, String> log = new TreeMap<Date, String>();
	
	BaseFortsMoney money = new BaseFortsMoney();
	double comission = 0;
	
	Map<TQSymbol, BaseFortsContract> positions = new HashMap<>();

	IFortsQuotesSupplier quotesSupplier;
	
	public FortsAccountSimulator(IFortsQuotesSupplier quotesSupplier, double current) {
		this.quotesSupplier = quotesSupplier;
		money.setCurrent(current);
		money.setFree(current);
	}
	
	private void log(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append(message + ":\n");
		sb.append(this.toString());
		log.put(new Date(), sb.toString());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FORTS Money: " + money + "\n");
		
		for (TQSymbol symbol : positions.keySet()) {
			BaseFortsContract contract = positions.get(symbol);
			sb.append(contract.toString() + "\n");
		}
		return sb.toString();
	}

	public double getFree() {
		return money.getFree();
	}
	
	public double getCurrent() {
		return money.getCurrent();
	}
	
	public double getBlocked() {
		return money.getBlocked();
	}
	
	public double getVarmargin() {
		return money.getVarmargin();
	}
	
	public int availableToBuy(TQSymbol symbol) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			BaseFortsContract newPosition = position.clone();
			int available = 0;
			while(newPosition.calcBuy(available+1) <= money.getFree()) {
				available++;
			}
			return available;
		} else {
			throw new RuntimeException(symbol + " not found!");
		}
	}
	

	public int availableToSell(TQSymbol symbol) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			BaseFortsContract newPosition = position.clone();
			int available = 0;
			while(newPosition.calcSell(available+1) <= money.getFree()) {
				available++;
			}
			return available;
		} else {
			throw new RuntimeException(symbol + " not found!");
		}
	}
	
	/**
	 * Постановка ордеров на покупку
	 * @param symbol
	 * @param quantity
	 */
	public void lockBuy(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		if (position == null) {
			position = new BaseFortsContract();
			positions.put(symbol, position);
		}
		
		position.setPrice(quotesSupplier.getSellPrice(symbol));
		
		double diffGO = position.calcBuy(quantity);
		
		// если свободных средств не хватает - не работаем
		if (money.getFree() < diffGO) {
			throw new RuntimeException("Cannot lock buy " + quantity + " contracts!\n" 
			+ "Position: " + position + "\n Money: " + money);
		} else {
			
			// Иначе - работаем
			position.setOpenbuys(position.getOpenbuys() + quantity);
			
			// Пересчитаем ГО и свободные
			money.setFree(money.getFree() - diffGO);
			money.setBlocked(money.getBlocked() + diffGO);
		}
	}
	
	/**
	 * Постановка ордеров на продажу
	 * @param symbol
	 * @param quantity
	 */
	public void lockSell(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null) {
			position = new BaseFortsContract();
			positions.put(symbol, position);
		}
		
		position.setPrice(quotesSupplier.getBuyPrice(symbol));
		
		double diffGO = position.calcSell(quantity);
		
		// если свободных средств не хватает - не работаем
		if (money.getFree() < diffGO) {
			throw new RuntimeException("Cannot lock sell " + quantity + " contracts!\n" 
					+ "Position: " + position + "\n Money: " + money);
		} else {
			// свободных хватает - работаем
			position.setOpensells(position.getOpensells() + quantity);
			
			money.setFree(money.getFree() - diffGO);
			money.setBlocked(money.getBlocked() + diffGO);
		}
	}
	
	/**
	 * Покупка из очереди на покупку
	 * @param symbol
	 * @param quantity
	 * @return
	 */
	public QuantityCost buyLocked(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null || position.getOpenbuys() < quantity) {
			throw new RuntimeException("В очереди нет " + quantity + " контрактов на покупку " + symbol);
		}
		double oldGO = position.getGO();
		position.setOpenbuys(position.getOpenbuys() - quantity);
		position.setTodaybuy(position.getTodaybuy() + quantity);
		double newGO = position.getGO();
		double diff = money.recalcGO(oldGO, newGO);
		return new QuantityCost(quantity, diff);
	}
	
	/**
	 * Продажа из очереди на продажу
	 * @param symbol
	 * @param quantity
	 */
	public QuantityCost sellLocked(TQSymbol symbol, int quantity) {
		BaseFortsContract position = positions.get(symbol);
		
		if (position == null || position.getOpensells() < quantity) {
			throw new RuntimeException("В очереди нет " + quantity + " контрактов на продажу " + symbol);
		}
		
		double oldGO = position.getGO();
		position.setOpensells(position.getOpensells() - quantity);
		position.setTodaysell(position.getTodaysell() + quantity);
		double newGO = position.getGO();
		double diff = money.recalcGO(oldGO, newGO);
		return new QuantityCost(quantity, diff); 
	}
	
	/**
	 * Моментальная покупка
	 * @param symbol
	 * @param quantity
	 */
	public QuantityCost buy(TQSymbol symbol, int quantity, double price) {
		log("Buy: " + symbol + " quantity:" + quantity);
		lockBuy(symbol, quantity);
		return buyLocked(symbol, quantity);
	}
	
	public QuantityCost sell(TQSymbol symbol, int quantity, double price) {
		log("Sell: " + symbol + " quantity:" + quantity);
		lockSell(symbol, quantity);
		return sellLocked(symbol, quantity);
	}
	
	/**
	 * Пересчитывает вариационную маржу
	 * @param symbol
	 * @param price
	 */
	public void recalc(TQSymbol symbol, double price) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			double diff = position.getPrice() - price;
			double varmargin = (position.getTotalnet() + Math.abs(position.getOpenbuys()-position.getOpensells())) * diff;
			position.setVarmargin(varmargin);
		}
	}
	
	public QuantityCost close(TQSymbol symbol, double price) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			recalc(symbol, price);
			if (position.getTotalnet() == 0) {
				// нет открытых контрактов
				return new QuantityCost(0, 0);
			} else if (position.getTotalnet() < 0) { // контракты на продажу
				int quantity = -position.getTotalnet();
				lockBuy(symbol, quantity);
				return buyLocked(symbol, quantity);
			} else {// контракты на покупку
				int quantity = position.getTotalnet();
				lockSell(symbol, quantity);
				return sellLocked(symbol, quantity);				
			}
		}
		return new QuantityCost(0, 0);
	}
	
	/**
	 * Снять открытые ордера
	 */
	public void cancelOpenOrders(TQSymbol symbol) {
		BaseFortsContract position = positions.get(symbol);
		if (position != null) {
			double oldGO = position.getGO();
			position.setOpenbuys(0);
			position.setOpensells(0);
			double newGO = position.getGO();
			money.recalcGO(oldGO, newGO);
		}
	}
	
	public void putOrder(TQSymbol symbol, BuySell buySell, int quantity, double price) {
		Order order = new Order();
		order.setTime(new Date());
		order.setBoard(symbol.getBoard());
		order.setSeccode(symbol.getSeccode());
		order.setQuantity(quantity);
		order.setPrice(price);
		order.setBuysell(buySell);
		lockBuy(symbol, quantity);

	}

	public void putBuyOrder(TQSymbol symbol, int quantity, double price) {
		putOrder(symbol, BuySell.B, quantity, price);
	}
	

	public void putSellOrder(TQSymbol symbol, int quantity, double price) {
		putOrder(symbol, BuySell.S, quantity, price);
	}
	
	public static void main(String[] args) {
		/*FortsAccountSimulator a = new FortsAccountSimulator(new IFortsQuotesSupplier() {
			
			@Override
			public double getSellPrice(TQSymbol symbol) {
				return 45.0;
			}
			
			@Override
			public double getBuyPrice(TQSymbol symbol) {
				return 55.0;
			}
			
			@Override
			public double getAvgPrice(TQSymbol symbol) {
				return 50.0;
			}
			
		}, 50);
		
		TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
		
		System.out.println(a);

		a.buy(symbol, 1);
		//a.sell(symbol, 1);
		
		System.out.println("-");
		System.out.println(a);

		
		int availBuy = a.availableToBuy(symbol);
		int availSell = a.availableToSell(symbol);
		System.out.println("available to buy : " + availBuy);
		System.out.println("available to sell : " + availSell);

		//a.lockSell(symbol, 1, 50.0);
		
		System.out.println("-");
		System.out.println(a);
		
		a.closePosition(symbol);
		a.cancelOpenOrders(symbol);
		
		System.out.println("-");
		System.out.println(a);*/
		
	}

	@Override
	public QuantityCost buy(TQSymbol symbol, int quantity, QuoteGlass quoteGlass) {
		throw new UnimplementedException();
	}

	@Override
	public QuantityCost sell(TQSymbol symbol, int quantity, QuoteGlass quoteGlass) {
		throw new UnimplementedException();
	}

	@Override
	public Map<TQSymbol, QuantityCost> getPositions() {
		throw new UnimplementedException();
	}

	@Override
	public void reset() {
		throw new UnimplementedException();
	}

	@Override
	public Map<TQSymbol, QuantityCost> getInitialPositions() {
		// TODO Auto-generated method stub
		return null;
	}


}
