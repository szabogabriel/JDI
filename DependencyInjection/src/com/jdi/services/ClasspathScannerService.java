package com.jdi.services;

import java.util.Set;

public interface ClasspathScannerService {
	
	Set<?> scanForPackages(String rootPackage, Class<?> supertype);

}
