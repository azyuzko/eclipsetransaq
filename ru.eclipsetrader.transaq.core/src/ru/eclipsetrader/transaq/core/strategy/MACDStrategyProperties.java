package ru.eclipsetrader.transaq.core.strategy;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.PriceType;

public class MACDStrategyProperties {

	int optInFastPeriod;
	int optInSlowPeriod;
	int optInSignalPeriod;		
	CandleType candleType;
	PriceType priceType;

}
