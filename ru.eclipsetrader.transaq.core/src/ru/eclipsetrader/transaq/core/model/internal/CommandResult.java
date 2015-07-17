package ru.eclipsetrader.transaq.core.model.internal;

import ru.eclipsetrader.transaq.core.util.Utils;

public class CommandResult {

	boolean success = false;
	String message;
	
	String transactionId;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String toString() {
		return Utils.toString(this);
	}
}
