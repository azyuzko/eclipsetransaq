package ru.eclipsetrader.transaq.core.model;

public enum BuySell {

	B, S;
	
	public BuySell reverted() {
		switch (this) {
		case B:	return S;
		case S:	return B;
		}
		return null;
	}
	
}
