package ru.eclipsetrader.transaq.core.helper;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TestTickList extends TestCase {

	TQSymbol symbol = TQSymbol.SiU5;
	Date fromDate = Utils.parseDate("03.08.2015 10:30:00.000");
	Date toDate = Utils.parseDate("03.08.2015 12:15:00.000");
	
	TickList tickListDB = new TickList();
	TickList tickListCommon = new TickList();
	
	Tick t1;
	Tick t2;
	Tick t3;
	Tick t4;
	
	@Override
	protected void setUp() throws Exception {
		//List<Tick> list = DataManager.getTickList(fromDate, toDate, new TQSymbol[] {symbol}).stream().map(t -> { return (Tick)t;} ).collect(Collectors.toList());
		//tickListDB.add(list);
		
		t1 = new TickTrade().setTime(DateUtils.addSeconds(fromDate, 15)).setBuysell(BuySell.S).setPrice(50.0).setQuantity(1);
		t2 = new TickTrade().setTime(DateUtils.addSeconds(fromDate, 20)).setBuysell(BuySell.S).setPrice(49.0).setQuantity(1);
		t3 = new TickTrade().setTime(DateUtils.addSeconds(fromDate, 25)).setBuysell(BuySell.S).setPrice(51.0).setQuantity(1);
		t4 = new TickTrade().setTime(DateUtils.addSeconds(fromDate, 30)).setBuysell(BuySell.S).setPrice(51.0).setQuantity(1);
		
	}
	
	@Test
	public void testSpeed() throws Exception {
		

	}
}
