package ru.eclipsetrader.transaq.core.orders;

import ru.eclipsetrader.transaq.core.model.internal.CommandResult;

public interface ITransaqCallback {
	void onTransaqError(CommandResult commandResult);
	void onError(String message);
}
