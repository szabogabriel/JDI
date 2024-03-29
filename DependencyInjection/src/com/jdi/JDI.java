package com.jdi;

import java.util.Optional;

import com.jdi.config.ConfigService;
import com.jdi.services.ClasspathScannerService;
import com.jdi.services.ServiceFactory;
import com.jdi.services.impl.ClasspathScannerServiceImpl;
import com.jdi.services.impl.ServiceFactoryImpl;

public class JDI {

	private ConfigService config = ConfigService.NULL_OBJECT;

	private ClasspathScannerService classpathScannerService = new ClasspathScannerServiceImpl();

	private ServiceFactory serviceFactory = null;
	
	public <T> Optional<T> getServiceImpl(Class<T> service) {
		return this.getServiceImpl(service, null);
	}
	
	public <T> Optional<T> getServiceImpl(Class<T> service, String discriminator) {
		return getServiceFactory().getServiceImpl(service, discriminator);
	}

	public void setConfigService(ConfigService configService) {
		this.config = configService;
		if (serviceFactory != null) {
			serviceFactory = null;
		}
	}

	public void setClasspathScannerService(ClasspathScannerService classpathScannerService) {
		this.classpathScannerService = classpathScannerService;
		if (serviceFactory != null) {
			serviceFactory = null;
		}
	}
	
	private ServiceFactory getServiceFactory() {
		if (serviceFactory == null) {
			synchronized(this) {
				if (serviceFactory == null) {
					serviceFactory = new ServiceFactoryImpl(config, classpathScannerService);
				}
			}
		}
		return serviceFactory;
	}

}
