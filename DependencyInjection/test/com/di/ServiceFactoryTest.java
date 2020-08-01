package com.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.di.data.ParamInterface;
import com.di.data.ParamInterfaceImpl;
import com.di.data.TestInterface;
import com.di.data.TestInterfaceImpl1;
import com.di.data.TestInterfaceImpl2;
import com.di.data.TestInterfaceImpl3;

public class ServiceFactoryTest {
	
	private MockConfigService configService;
	private ServiceFactory serviceFactory;
	
	@Before
	public void prepare() {
		configService = new MockConfigService();
		serviceFactory = new ServiceFactoryImpl(configService);
	}
	
	@Test
	public void testNullClassParameter() {
		Optional<Object> ret = serviceFactory.getServiceImpl(null);
		
		assertFalse(ret.isPresent());
	}
	
	@Test
	public void testCreateInstanceOfClassWithNoConstructor() {
		Optional<String> tmp = serviceFactory.getServiceImpl(String.class);
		
		assertTrue(tmp.isPresent());
		assertEquals("", tmp.get());
	}
	
	@Test
	public void testCreateInstanceOfServiceImplWithNoConstructorSingleton() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl1.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
		
		int hash1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertEquals(hash1, tmp.get().hashCode());
	}
	
	@Test
	public void testCreateInstanceOfServiceImplWithNoConstructorMultiton() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl1.class.getName());
		configService.set(ServiceFactory.PREFIX_TYPE + TestInterface.class.getName(), "Multiton");
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
		
		int hash1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotEquals(hash1, tmp.get().hashCode());
	}
	
	@Test
	public void testCreateInstanceOfServiceWithConfigServiceAttribute() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl2.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
		
		int hash1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertEquals(hash1, tmp.get().hashCode());
	}
	
	@Test
	public void testCreateInstanceOfServiceImplWithMultipleAttributes() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl3.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + ParamInterface.class.getName(), ParamInterfaceImpl.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
	}
	
}
