package ru.eclipsetrader.transaq.core.model.internal;


public class FortsMoneyPositionId {

	String client;
	Integer market;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FortsMoneyPositionId) {
			FortsMoneyPositionId id = (FortsMoneyPositionId)obj;
			return client.equals(id.client) && market.equals(id.market);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return client.hashCode() + market.hashCode();
	}

}
