package com.di;

public enum ServiceClassType {
	
	STATELESS,
	STATEFUL,
	;
	
	public static ServiceClassType getType(String type) {
		ServiceClassType ret = STATELESS;
		
		for (ServiceClassType it : values()) {
			if (it.name().equals(type)) {
				ret = it;
			}
		}
		
		return ret;
	}
	
}
