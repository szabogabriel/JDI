package com.di;

import java.util.Optional;

public interface ConfigService {
	
	Optional<String> get(String key);
	
	public static ConfigService CONFIG_SERVICE = null;
	
	public static ConfigService NULL_OBJECT = new ConfigService() {

		@Override
		public Optional<String> get(String key) {
			return Optional.empty();
		}
		
	};

}
