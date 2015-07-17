package ru.eclipsetrader.transaq.core.event;

public abstract interface Observer<T> {
	public abstract void update(T paramObject);
}