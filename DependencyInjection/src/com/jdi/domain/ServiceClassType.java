package com.jdi.domain;

public enum ServiceClassType {
	
	MULTITON,
	SINGLETON,
	;
	
	public static ServiceClassType getType(String type) {
		ServiceClassType ret = SINGLETON;
		
		for (ServiceClassType it : values()) {
			if (it.name().equalsIgnoreCase(type)) {
				ret = it;
			}
		}
		
		return ret;
	}
	
}
