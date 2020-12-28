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
import java.util.stream.Collectors;

public class ServiceFactoryImpl implements ServiceFactory {

	private final ConfigService CONFIG;
	
	private final Map<String, Object> CACHE = new HashMap<>();
	
	public ServiceFactoryImpl(ConfigService configService) {
		CONFIG = configService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getServiceImpl(Class<T> service) {
		Optional<T> ret = Optional.empty();
		if (service != null) {
			Optional<String> implementationToCreate = getClassToInstantiate(service);
			if (implementationToCreate.isPresent()) {
				String implToCreate = implementationToCreate.get();
				if (isInCache(implToCreate)) {
					ret = getFromCache(implToCreate);					
				} else {
					try {
						Object newInstance = createInstance(implToCreate);
						if (isSingleton(service)) {
							storeInCache(newInstance.getClass(), newInstance);
						}
						ret = Optional.of((T)newInstance);
					} catch (ClassNotFoundException |InstantiationException | IllegalAccessException  ex) {
						ex.printStackTrace();
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
			ret = clss.newInstance();
		} else {
			ret = createInstance(constructors);
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
				ret = null;
			}
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
				Optional<?> toCreate = getServiceImpl(params[j].getType());
				if (toCreate.isPresent()) {
					paramsToSend[j] = toCreate.get();
				} else {
					paramsToSend[j] = null;
				}
			}
		}
		
		return paramsToSend;
	}
	
	private List<Constructor<?>> getPublicConstructors(Constructor<?> [] constructors) {
		return Arrays.asList(constructors).parallelStream().filter(c -> Modifier.isPublic(c.getModifiers())).collect(Collectors.toList());
	}
	
	private boolean isPublicZeroArgumentConstructorPresent(List<Constructor<?>> constructors) {
		return constructors.parallelStream().filter(c -> c.getParameterCount() == 0).findAny().isPresent();
	}
	
	private Optional<String> getClassToInstantiate(Class<?> clss) {
		Optional<String> implementationToCreate = Optional.empty();
		if (isInterfaceOrAbstractClass(clss)) {
			implementationToCreate = getServiceClassFromConfig(clss);
		} else {
			implementationToCreate = Optional.of(clss.getCanonicalName());
		}
		return implementationToCreate;
	}
	
	private Optional<String> getServiceClassFromConfig(Class<?> clss) {
		return CONFIG.get(PREFIX_IMPL + clss.getCanonicalName());
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
