package com.di;

import java.util.Optional;

public interface ServiceFactory {
	
	public static final String PREFIX_IMPL = "impl.";
	public static final String PREFIX_TYPE = "type.";
	
	public static final String VALUE_MULTITON = "MULTITON";
	
	public <T> Optional<T> getServiceImpl(Class<T> service);

}
