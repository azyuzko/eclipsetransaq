package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.model.BuySell;

public class Account {

	private static final double COMISSION_RATE = 0.0001; // % от суммы сделки
	
	double initialCash;
	double cash;
	int position;
	
	double comission = 0;
	
	public Account(double cash, int position) {
		this.cash = cash;
		this.initialCash = cash;
		this.position = position;
	}
	
	public double getCash() {
		return cash;
	}

	public double getComission() {
		return comission;
	}

	public int getPosition() {
		return position;
	}
	
	public String toString() {
		return "Position: " + position  + "  Cash = " + cash + " comission = " + comission; 
	}
	
	public int canBuy(double price) {
		if (cash <= 0) {
			return 0;
		}
		return (int) (cash / price); 
	}
	
	public int canSell(double price) {
		if (position <= 0) {
			return 0;
		}
		return (int) position; 
	}
	
	public int available(BuySell buySell, double price) {
		if (buySell  == BuySell.B) {
			return canBuy(price);
		} else {
			return canSell(price);
		}
	}
	
	public void buysell(BuySell buySell, double price, int quantity) {
		if (buySell == BuySell.B) {
			buy(price, quantity);
		} else {
			sell(price, quantity);
		}
	}
	
	public void buy(double price, int quantity) {
		if (quantity == 0 || price == 0) {
			System.err.println("wrong signal quantity = " + quantity + " price = " + price);
			return;
		}
		if (quantity > 0) {
			//System.out.println("buy: " + quantity + " (" + price + ")");
		} else {
			//System.out.println("sell: " + quantity + " (" + price + ")");
		}
		cash -= price * quantity;
		position += quantity;
		double currentComission = Math.abs(price * quantity) * COMISSION_RATE;
		comission += currentComission;
		cash -= currentComission;
		cash = Math.round(cash * 100) / 100;
	}
	
	public void sell(double price, int quantity) {
		buy(price, -quantity);
	}
	
	public void close(double buyPrice, double sellPrice) {
		if (position > 0) {
			sell(sellPrice, position);
		} else if (position < 0) {
			buy(buyPrice, position);
		}
	}
	
	/**
	 * Оценка счета
	 * Свободные средства + оценка позиции по курсу
	 * @param price курс оценки
	 * @return
	 */
	public double getEstimate(double price) {
		return cash + price * position;
	}
	
	public double profit() {
		if (position != 0) {
			throw new RuntimeException("Закройте позиции для оценки эффективности");
		}
		return Math.round((cash)/initialCash*100)/100; 
	}
	
	public static void main(String[] args) {
		Account a = new Account(1000, 0);
		
		System.out.println(a);
		
		a.buy(10, 4);
		
		System.out.println(a);
		
		a.sell(13, 3);
		
		System.out.println(a);
	
		a.close(12, 12);
		
		System.out.println(a);
	}

}
