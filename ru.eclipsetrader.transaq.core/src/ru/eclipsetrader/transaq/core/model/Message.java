package ru.eclipsetrader.transaq.core.model;

import java.util.Date;

public class Message {

	Date date;
	boolean isUrgent = false;
	String text;
	String from;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isUrgent() {
		return isUrgent;
	}

	public void setUrgent(boolean isUrgent) {
		this.isUrgent = isUrgent;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@Override
	public int hashCode() {
		if (text != null) {
			return text.hashCode();
		}
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (text != null) {
			return text.equals(obj);
		}
		return super.equals(obj);
	}
}
