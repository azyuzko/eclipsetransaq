package ru.eclipsetrader.transaq.core.candle;

import ru.eclipsetrader.transaq.core.candle.CandleList.ICandleProcessContext;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class CandleTest {
public static void main(String[] args) {
		
		//Date dt1 = Utils.parseDate("15.02.2015 21:47:01.145");
		//System.out.println(Utils.formatDate(closestCandleStartTime(dt1, CandleType.CANDLE_61S)));
		//System.out.println(DateUtils.ceiling(dt1, Calendar.MINUTE));

		ICandleProcessContext candleProcessContext = new ICandleProcessContext() {

			@Override
			public void onCandleClose(Candle candle) {
				//System.out.println("Close " + candle);
			}
		};

		CandleList cl = new CandleList(CandleType.CANDLE_11S);
		Tick t1 = new TickTrade();
		t1.setTime(Utils.parseDate("15.02.2015 21:47:01.145"));
		t1.setQuantity(1);
		t1.setPrice(100.0);
		
		Tick t2 = new TickTrade();
		t2.setTime(Utils.parseDate("15.02.2015 21:47:10.145"));
		t2.setQuantity(2);
		t2.setPrice(200.0);

		Tick t3 = new TickTrade();
		t3.setTime(Utils.parseDate("15.02.2015 21:47:10.555"));
		t3.setQuantity(3);
		t3.setPrice(50.0);

		Tick t4 = new TickTrade();
		t4.setTime(Utils.parseDate("15.02.2015 21:47:11.130"));
		t4.setQuantity(1);
		t4.setPrice(500.0);

		Tick t5 = new TickTrade();
		t5.setTime(Utils.parseDate("15.02.2015 21:47:11.503"));
		t5.setQuantity(2);
		t5.setPrice(400.0);

		cl.processTickInCandle(t1, candleProcessContext);
		System.out.println("1 " + cl);
		cl.processTickInCandle(t2, candleProcessContext);
		System.out.println("2 " + cl);
		cl.processTickInCandle(t3, candleProcessContext);
		System.out.println("3 " + cl);
		cl.processTickInCandle(t4, candleProcessContext);
		System.out.println("4 " + cl);
		cl.processTickInCandle(t5, candleProcessContext);
		System.out.println("5 " + cl);
	}
}
