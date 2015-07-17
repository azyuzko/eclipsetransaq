package ru.eclipsetrader.transaq.core.server;

public enum LogLevel {

	MIN, STANDARD, MAX;
	
	public int getLevel() {
		switch (this) {
		case MIN:
			return 1;
		case STANDARD:
			return 2;
		case MAX:
			return 3;
		}
		return 2;
	}

}
