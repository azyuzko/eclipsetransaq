package ru.eclipsetrader.transaq.core.account;


/**
 * quantity - кол-во контактов
 * cost - уплаченная за них сумма
 * 
 * @author Zyuzko-AA
 * 
 */
public class QuantityCost {

	int quantity;
	double cost;

	public QuantityCost(int quantity, double cost) {
		this.quantity = quantity;
		this.cost = cost;
	}
	
	@Override
	public String toString() {
		return "Quantity: " + quantity + ", Cost: " + cost;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	
}
