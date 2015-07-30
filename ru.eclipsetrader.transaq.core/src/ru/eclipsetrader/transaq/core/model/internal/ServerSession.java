package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

@Entity
@Table(name = "server_session")
public class ServerSession {

	@Id
	@Column(name="SESSION_ID")
	String sessionId;
	@Temporal(TemporalType.TIMESTAMP)
	Date connected;
	@Temporal(TemporalType.TIMESTAMP)
	Date disconnected;
	String serverId;
	String timezone;
	String error;

	@Enumerated(EnumType.STRING)
	ConnectionStatus status = ConnectionStatus.DISCONNECTED;
	
	public ServerSession() {
		super();
	}
	
	public ServerSession(String serverId) {
		super();
		setServerId(serverId);
	}

	@Id
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public Date getConnected() {
		return connected;
	}

	public void setConnected(Date connected) {
		this.connected = connected;
	}

	public Date getDisconnected() {
		return disconnected;
	}

	public void setDisconnected(Date disconnected) {
		this.disconnected = disconnected;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}

}
