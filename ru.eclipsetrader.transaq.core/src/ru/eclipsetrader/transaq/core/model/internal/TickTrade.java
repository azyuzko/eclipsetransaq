package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Table;
import javax.persistence.Entity;

import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
@Table(name="ticks")
public class TickTrade extends Tick {

	public TickTrade() {
		super();
	}

	public TickTrade(String serverId) {
		super(serverId);
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}
}
