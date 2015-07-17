package ru.eclipsetrader.transaq.core.data;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class DefaultJPAListener {
	
	@PrePersist
	@PreUpdate
	public void prePersist(Object object) {
		
	}

}
