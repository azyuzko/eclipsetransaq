package ru.eclipsetrader.transaq.core.model.internal;

public class FortsPositionId {
	String seccode;
	Integer market;
	String client;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FortsPositionId) {
			FortsPositionId id = (FortsPositionId) obj;
			return seccode.equals(id.seccode) && (market == id.market) && client.equals(id.market);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return seccode.hashCode() + market.hashCode() + client.hashCode();
	}
}
