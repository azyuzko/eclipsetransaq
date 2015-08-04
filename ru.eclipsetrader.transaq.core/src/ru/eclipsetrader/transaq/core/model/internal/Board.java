package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Id;

import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
public class Board  extends ServerObject {

	@Id
	String id;
	String name;
	String market;
	String type;
	
	public Board() {
		super(null);
	}
	
	public Board(String serverId) {
		super(serverId);
	}
	
	public String getKey() {
		return id;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		return Utils.toString(this);
	}
}
