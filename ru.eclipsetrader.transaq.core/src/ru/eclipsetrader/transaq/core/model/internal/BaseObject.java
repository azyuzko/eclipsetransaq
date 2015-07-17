package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.MappedSuperclass;

import ru.eclipsetrader.transaq.core.interfaces.ITQKey;
import ru.eclipsetrader.transaq.core.util.Utils;

@MappedSuperclass
public abstract class BaseObject extends SessionObject implements ITQKey {
	

	/**
	 * Ключ объекта, уникальный идентификатор
	 * Поле или связка полей.
	 * Обязательно для опереледения
	 * @return
	 */
	public abstract String getKey();
	
	public String toString() {
		return Utils.toString(this);
	}
	
}
