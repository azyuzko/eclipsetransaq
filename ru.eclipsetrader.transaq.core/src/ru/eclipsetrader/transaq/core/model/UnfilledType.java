package ru.eclipsetrader.transaq.core.model;

public enum UnfilledType {
	/**
	 * неисполненная часть заявки помещается в очередь заявок Биржи
	 */
	PutInQueue,
	
	/**
	 * сделки совершаются только в том случае, если заявка может быть удовлетворена полностью
	 */
	CancelBalance,
	
	/**
	 * неисполненная часть заявки снимается с торгов
	 */
	ImmOrCancel;
}
