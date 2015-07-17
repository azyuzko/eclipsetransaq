package ru.eclipsetrader.transaq.core.interfaces;

import java.util.List;

public interface ICustomStorage<ID, Type> {
	
	public void clear();
	
	public void put(ID id, Type object);
	
	public void putList(List<Type> objects);
	
	public Type get(ID id);

	public List<Type> getAll();
	
}
