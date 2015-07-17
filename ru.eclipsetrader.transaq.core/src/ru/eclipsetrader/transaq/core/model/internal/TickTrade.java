package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Table;

import javax.persistence.Entity;

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
