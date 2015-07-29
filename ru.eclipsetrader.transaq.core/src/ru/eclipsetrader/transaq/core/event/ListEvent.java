package ru.eclipsetrader.transaq.core.event;

import java.util.List;

public class ListEvent<T> extends Event<List<T>> {

	public ListEvent(String name) {
		super(name);
	}
	
}
