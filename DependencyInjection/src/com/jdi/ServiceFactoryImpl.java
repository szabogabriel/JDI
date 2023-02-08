package com.jdi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.jdi.annotations.Discriminator;
import com.jdi.util.ClassScanner;

public class ServiceFactoryImpl implements ServiceFactory {

	private final ConfigService CONFIG;
	
	private final Map<String, Object> CACHE = new HashMap<>();
	
	public ServiceFactoryImpl(ConfigService configService) {
		CONFIG = configService;
	}
	
	public synchronized <T> Optional<T> getServiceImpl(Class<T> service) {
		return this.getServiceImpl(service, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> Optional<T> getServiceImpl(Class<T> service, String discriminator) {
		Optional<T> ret = Optional.empty();
		if (service != null) {
			System.out.println("Accessing service: " + service.getCanonicalName());
			if (isEnumeration(service)) {
				Enum<?> retEnum = Enum.valueOf((Class<? extends Enum>)service, discriminator);
				ret = Optional.of((T)retEnum);
			} else {
			Optional<String> implementationToCreate = getClassToInstantiate(service, discriminator);
				if (implementationToCreate.isPresent()) {
					String implToCreate = implementationToCreate.get();
					if (isInCache(implToCreate)) {
						System.out.println("Service " + service.getCanonicalName() + " loaded from cache.");
						ret = getFromCache(implToCreate);					
					} else {
						System.out.println("Creating new instance of service " + service.getCanonicalName() + ".");
						try {
							Object newInstance = createInstance(implToCreate);
							ret = Optional.ofNullable((T)newInstance);
							if (isSingleton(service) && ret.isPresent()) {
								storeInCache(newInstance.getClass(), newInstance);
							}
							if (ret.isEmpty()) {
								System.err.println("Couldn't create instance of " + implToCreate + ".");
							}
						} catch (ClassNotFoundException |InstantiationException | IllegalAccessException  ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
		return ret;
	}
	
	private void storeInCache(Class<?> clss, Object o) {
		CACHE.put(clss.getName(), o);
	}
	
	private Object createInstance(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object ret = null;
		Class<?> clss = Class.forName(name);
		
		List<Constructor<?>> constructors = getPublicConstructors(clss.getConstructors());
		
		if (isPublicZeroArgumentConstructorPresent(constructors)) {
			System.out.println("Creating new instance of " + name + " with default constructor.");
			ret = clss.newInstance();
		} else {
			System.out.println("Creating new instance of " + name + " with parameterized constructor.");
			ret = createInstance(constructors);
		}
		if (ret != null) {
			System.out.println("New instance of " + name + " created.");
		}
		return ret;
	}
	
	private Object createInstance(List<Constructor<?>> constructors) {
		Object ret = null;
		
		constructors.sort((c1, c2) -> c1.getParameterCount() - c2.getParameterCount());
		
		int i = 0;
		while (i < constructors.size() && ret == null) {
			ret = createInstance(constructors.get(i));
			i++;
		}
		
		return ret;
	}
	
	private Object createInstance(Constructor<?> constructor) {
		Object ret = null;
		Parameter [] params = constructor.getParameters();
		Object [] paramsToSend = createParameterInstances(params);
		
		if (isEveryParameterSet(paramsToSend)) {
			try {
				ret = constructor.newInstance(paramsToSend);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
				ret = null;
			}
		} else {
			System.err.println("Missing constructor parameters.");
		}
		return ret;
	}
	
	private boolean isEveryParameterSet(Object[] params) {
		for (Object it : params) {
			if (it == null) {
				return false;
			}
		}
		return true;
	}
	
	private Object[] createParameterInstances(Parameter[] params) {
		Object[] paramsToSend = new Object[params.length];
		
		for (int j = 0; j < params.length; j++) {
			if (params[j].getType().equals(ConfigService.class)) {
				paramsToSend[j] = CONFIG;
			} else {
				Optional<?> toCreate;
				Optional<String> instanceName = getDiscriminator(params[j]);

				if (instanceName.isPresent()) {
					toCreate = getServiceImpl(params[j].getType(), instanceName.get());
				}
				else {
					toCreate = getServiceImpl(params[j].getType());
				}
				
				if (toCreate.isPresent()) {
					paramsToSend[j] = toCreate.get();
				} else {
					System.err.println("Missing parameter: " + params[j].getType());
					paramsToSend[j] = null;
				}
			}
		}
		
		return paramsToSend;
	}
	
	private Optional<String> getDiscriminator(Parameter param) {
		Optional<String> ret = Optional.empty();
		if (param.isAnnotationPresent(Discriminator.class)) {
			Discriminator disc = param.getAnnotation(Discriminator.class);
			if (disc != null) {
				ret = Optional.of(disc.value());
			}
		}
		return ret;
	}
	
	private List<Constructor<?>> getPublicConstructors(Constructor<?> [] constructors) {
		return Arrays.asList(constructors).parallelStream().filter(c -> Modifier.isPublic(c.getModifiers())).collect(Collectors.toList());
	}
	
	private boolean isPublicZeroArgumentConstructorPresent(List<Constructor<?>> constructors) {
		return constructors.parallelStream().filter(c -> c.getParameterCount() == 0).findAny().isPresent();
	}
	
	private Optional<String> getClassToInstantiate(Class<?> clss, String discriminator) {
		Optional<String> implementationToCreate = Optional.empty();
		
		implementationToCreate = getServiceClassFromConfig(clss, discriminator);
		if (implementationToCreate.isEmpty()) {
			if (isInterfaceOrAbstractClass(clss)) {
				implementationToCreate = getServiceClassFromClasspathScan(clss, discriminator);
			} else {
				implementationToCreate = Optional.of(clss.getCanonicalName());
			}
		}
		return implementationToCreate;
	}
	
	private Optional<String> getServiceClassFromClasspathScan(Class<?> clss, String discriminator) {
		Optional<String> ret = Optional.empty();
		Optional<String> packageToScan = CONFIG.getPackageScanRoot();
		if (packageToScan.isPresent()) {
			Set<?> classes = ClassScanner.scanForPackages(packageToScan.get(), clss);
			if (classes != null && !classes.isEmpty()) {
				for (Object it : classes) {
					ret = Optional.of(((Class<?>)it).getName());
				}
			}
		}
		return ret;
	}
	
	private Optional<String> getServiceClassFromConfig(Class<?> clss, String discriminator) {
		String discriminatorPrefix = (discriminator == null) ? "" : discriminator + (discriminator.endsWith(".") ? "" : ".");
		return CONFIG.get(discriminatorPrefix + PREFIX_IMPL + clss.getCanonicalName());
	}
	
	@SuppressWarnings("unchecked")
	private <T> Optional<T> getFromCache(String className) {
		return Optional.of((T) CACHE.get(className));
	}
	
	private boolean isInterfaceOrAbstractClass(Class<?> clss) {
		return
			clss.isInterface() ||
			Modifier.isAbstract(clss.getModifiers());
	}
	
	private boolean isEnumeration(Class<?> clss) {
		return clss.isEnum();
	}
	
	private boolean isSingleton(Class<?> clss) {
		Optional<String> type = CONFIG.get(PREFIX_TYPE + clss.getCanonicalName());
		return 
				!type.isPresent() || 
				ServiceClassType.getType(type.get()).equals(ServiceClassType.SINGLETON);
	}
	
	private boolean isInCache(String className) {
		return CACHE.containsKey(className);
	}
	
}
