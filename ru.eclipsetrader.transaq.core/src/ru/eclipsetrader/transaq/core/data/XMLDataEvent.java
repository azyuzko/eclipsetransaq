package ru.eclipsetrader.transaq.core.data;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.xml.handler.XMLProcessType;

@Entity
@Table(name="event_audit")
public class XMLDataEvent {

	public enum Direction {
		IN, OUT;
	}
	
	@Id
	Long id;
	
	@Column(name="session_id")
	String sessionId;
	
	@Enumerated(EnumType.STRING)
	Direction direction;
	
	String data;
	
	@Column(name="event_time")
	Timestamp eventTime;
	
	@Enumerated(EnumType.STRING)
	XMLProcessType operation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	public XMLProcessType getOperation() {
		return operation;
	}

	public void setOperation(XMLProcessType operation) {
		this.operation = operation;
	}

}
