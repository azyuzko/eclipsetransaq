package ru.eclipsetrader.transaq.core.interfaces;

public interface IPersistable {

	public void persist();
	public void load(String serverId);
	
}
