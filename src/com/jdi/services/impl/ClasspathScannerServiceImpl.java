package com.jdi.services.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jdi.services.ClasspathScannerService;

public class ClasspathScannerServiceImpl implements ClasspathScannerService {

	private final char DOT = '.';
	private final char SLASH = '/';
	private final String CLASS_SUFFIX = ".class";

	private Map<String, List<Class<?>>> classesOnClasspath = new HashMap<>();

	public Set<?> scanForPackages(String rootPackage, Class<?> supertype) {
		initClassList(rootPackage);

		Set ret = new HashSet<>();

		for (Class<?> it : classesOnClasspath.get(rootPackage)) {
			if (supertype.isAssignableFrom(it) && !supertype.getName().equals(it.getName())) {
				ret.add(it);
			}
		}

		return ret;
	}
	
	private void initClassList(String rootPackage) {
		if (classesOnClasspath.size() == 0) {
			synchronized (ClasspathScannerServiceImpl.class) {
				if (classesOnClasspath.size() == 0) {
					try {
						classesOnClasspath.put(rootPackage, findClassesInClasspath(rootPackage));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private List<Class<?>> findClassesInClasspath(String packageName) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace(DOT, SLASH);
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				String rootPackage = (packageName != null && packageName.length() > 0) ? (packageName + DOT) : "";
				classes.addAll(findClasses(file, rootPackage + file.getName()));
			} else if (file.getName().endsWith(CLASS_SUFFIX)) {
				String className = file.getName().substring(0, file.getName().length() - CLASS_SUFFIX.length());
				if (className.contains("$")) {
					int pos = className.indexOf("$");
					String mainClassName = className.substring(0, pos);
					String nestedClassName = className.substring(pos + 1);
					Class<?> mainClass = Class.forName(packageName + DOT + mainClassName);
					Class<?>[] declaredClasses = mainClass.getDeclaredClasses();
					if (declaredClasses != null && declaredClasses.length > 0) {
						classes.add(declaredClasses[Integer.parseInt(nestedClassName) - 1]);
					}
				} else {
					classes.add(Class.forName(packageName + DOT + className));
				}
			}
		}
		return classes;
	}

}
