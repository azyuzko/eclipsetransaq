package ru.eclipsetrader.transaq.core.strategy;

import java.util.List;
import java.util.concurrent.Callable;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.trades.DataFeeder;

public class MACDStrategyFeederTask implements Callable<MACDStrategy> {
	
	private List<TickTrade> list;
	private MACDStrategyProperties properties;
	private TQSymbol symbol;
	public MACDStrategyFeederTask(TQSymbol symbol, List<TickTrade> list, MACDStrategyProperties props) {
		this.symbol = symbol;
		this.list = list;
		this.properties = props;
	}

	@Override
	public MACDStrategy call() {
		Instrument i = new Instrument(symbol, new CandleType[]{properties.candleType});
		DataFeeder feeder = new DataFeeder(list);
		MACDStrategy macdStrategy = new MACDStrategy(properties);	
		feeder.feed(i, macdStrategy);
		return macdStrategy;
	}

}
