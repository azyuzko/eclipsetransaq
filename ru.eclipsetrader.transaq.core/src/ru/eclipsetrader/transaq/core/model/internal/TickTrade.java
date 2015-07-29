package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="ticks")
public class TickTrade extends Tick {

	public TickTrade() {
		super();
	}

	public TickTrade(String serverId) {
		super(serverId);
	}

}
