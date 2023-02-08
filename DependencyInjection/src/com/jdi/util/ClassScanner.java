package com.jdi.util;

import java.util.Set;

import org.reflections.Reflections;

public class ClassScanner {
	
	public static Set<?> scanForPackages(String rootPackage, Class<?> supertype) {
		Reflections reflections = new Reflections(rootPackage);

		Set<?> subTypes = reflections.getSubTypesOf(supertype);

		return subTypes;
	}

}
