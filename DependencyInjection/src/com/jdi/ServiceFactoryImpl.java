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
				if (!isInCache(implementationToCreate.get())) {
					try {
						Object newInstance = createInstance(implementationToCreate.get());
						if (isSingleton(service)) {
							storeInCache(newInstance.getClass(), newInstance);
						}
						ret = Optional.of((T)newInstance);
					} catch (ClassNotFoundException |InstantiationException | IllegalAccessException  ex) {
						ex.printStackTrace();
					}
				} else {
					ret = getFromCache(implementationToCreate.get());
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
		Optional<Constructor<?>> zeroArgument = getPublicZeroArgumentConstructor(constructors);
		
		if (zeroArgument.isPresent()) {
			ret = clss.newInstance();
		} else {
			constructors.sort((c1, c2) -> c1.getParameterCount() - c2.getParameterCount());
			int i = 0;
			while (i < constructors.size() && ret == null) {
				Parameter [] params = constructors.get(i).getParameters();
				Object [] paramsToSend = new Object[params.length];

				boolean shouldProgress = true;
				
				for (int j = 0; j < params.length && shouldProgress; j++) {
					if (params[i].getType().equals(ConfigService.class)) {
						paramsToSend[i] = CONFIG;
					} else {
						Optional<?> toCreate = getServiceImpl(params[i].getType());
						if (toCreate.isPresent()) {
							paramsToSend[i] = toCreate.get();
						} else {
							shouldProgress = false;
						}
					}
				}
				
				if (shouldProgress) {
					try {
						ret = constructors.get(i).newInstance(paramsToSend);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						shouldProgress = false;
						ret = null;
					}
				}
			}
		}
		
		return ret;
	}
	
	private List<Constructor<?>> getPublicConstructors(Constructor<?> [] constructors) {
		return Arrays.asList(constructors).parallelStream().filter(c -> Modifier.isPublic(c.getModifiers())).collect(Collectors.toList());
	}
	
	private Optional<Constructor<?>> getPublicZeroArgumentConstructor(List<Constructor<?>> constructors) {
		return constructors.parallelStream().filter(c -> c.getParameterCount() == 0).findAny();
	}
	
	private Optional<String> getClassToInstantiate(Class<?> clss) {
		Optional<String> implementationToCreate = Optional.empty();
		if (isInterfaceOrAbstractClass(clss)) {
			implementationToCreate = getServiceClassFromConfig(clss);
		} else {
			implementationToCreate = Optional.of(clss.getName());
		}
		return implementationToCreate;
	}
	
	private Optional<String> getServiceClassFromConfig(Class<?> clss) {
		return CONFIG.get(PREFIX_IMPL + clss.getName());
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
		Optional<String> type = CONFIG.get(PREFIX_TYPE + clss.getName());
		return 
				!type.isPresent() || 
				!VALUE_MULTITON.equalsIgnoreCase(type.get());
	}
	
	private boolean isInCache(String className) {
		return CACHE.containsKey(className);
	}
	
}
