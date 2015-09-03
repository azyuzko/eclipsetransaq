package ru.eclipsetrader.transaq.core.orders;

import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.util.Holder;

public class DiffMap {

	Map<String, Holder<Object, Object>> map = new HashMap<>();
	
	public void put(String attrName, Object oldValue, Object newValue) {
		map.put(attrName, new Holder<Object, Object>(oldValue, newValue));
	}
	
	public Holder<Object, Object> get(String attr) {
		return map.get(attr);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		map.forEach((key,v) -> sb.append(key + ": " + v.getFirst() + " - " + v.getSecond() + "\n"));
		return sb.toString();
	}

}
