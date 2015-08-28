package ru.eclipsetrader.transaq.core.trades;

import ru.eclipsetrader.transaq.core.instruments.Instrument;

public interface IFeedContext {

	void OnStart(Instrument[] instruments);

}
