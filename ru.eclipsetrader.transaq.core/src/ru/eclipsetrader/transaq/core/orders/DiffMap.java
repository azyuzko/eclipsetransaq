package ru.eclipsetrader.transaq.core.orders;

import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.util.Holder;

public class DiffMap {

	Map<String, Holder<Object, Object>> map = new HashMap<>();
	
	public void put(String attrName, Object oldValue, Object newValue) {
		map.put(attrName, new Holder<Object, Object>(oldValue, newValue));
	}

}
