package ru.eclipsetrader.transaq.core.services;

import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.interfaces.ITQPosition;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;


public interface ITQPositionService extends IPersistable {

	public void clear();
	
	public ITQPosition getPosition(ITQSecurity security);

}
