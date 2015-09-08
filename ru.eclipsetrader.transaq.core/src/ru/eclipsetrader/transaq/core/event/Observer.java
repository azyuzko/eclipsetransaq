package ru.eclipsetrader.transaq.core.event;

@FunctionalInterface
public abstract interface Observer<T> {
	public abstract void update(T paramObject);
}