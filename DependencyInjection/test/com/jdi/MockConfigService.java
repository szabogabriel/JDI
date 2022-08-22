package com.jdi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MockConfigService implements ConfigService {
	
	public static final String MAPPING_TEST_PLACEHOLDER = "MAPPING_TEST_PLACEHOLDER";

	private Map<String, String> valuesToReturn = new HashMap<>();
	
	private int callCounter = 0;
	
	@Override
	public Optional<String> get(String key) {
		callCounter++;
		Optional<String> ret = Optional.empty();
		if (valuesToReturn.containsKey(key)) {
			ret = Optional.of(valuesToReturn.get(key));
		}
		return ret;
	}
	
	public void set(String key, String value) {
		valuesToReturn.put(key, value);
	}
	
	public void remove(String key) {
		valuesToReturn.remove(key);
	}
	
	public int getCallCounter() {
		return callCounter;
	}
	
	public void resetCallCounter() {
		callCounter = 0;
	}

}
