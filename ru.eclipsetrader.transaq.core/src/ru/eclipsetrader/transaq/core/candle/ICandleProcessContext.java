package ru.eclipsetrader.transaq.core.candle;


@FunctionalInterface
public interface ICandleProcessContext {
	void onCandleClose(CandleList candleList, Candle closedCandle);
}
