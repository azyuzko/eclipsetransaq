package ru.eclipsetrader.transaq.core.model.internal;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;


public class ServerStatus {

	Integer id;
	String timeZone;
	ConnectionStatus status;
	String error;
	
	public boolean isErrored() {
		return error != null && error.length() > 0;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public boolean equals(ServerStatus s1, ServerStatus s2) {
		if (s1 == null || s2 == null) {
			throw new IllegalArgumentException();
		}
		return s1.getStatus().equals(s2.getStatus());
	}

}
