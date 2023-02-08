package com.jdi;

import java.util.Optional;

public interface ConfigService {
	
	Optional<String> get(String key);
	
	default Optional<String> getPackageScanRoot() {
		return Optional.empty();
	}
	
	public static ConfigService CONFIG_SERVICE = null;
	
	public static ConfigService NULL_OBJECT = new ConfigService() {

		@Override
		public Optional<String> get(String key) {
			return Optional.empty();
		}
		
	};

}
