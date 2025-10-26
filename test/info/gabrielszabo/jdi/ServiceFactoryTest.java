package info.gabrielszabo.jdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.gabrielszabo.jdi.data.MultiParamConstructor;
import info.gabrielszabo.jdi.data.MyEnum;
import info.gabrielszabo.jdi.data.ParamInterface;
import info.gabrielszabo.jdi.data.ParamInterfaceImpl;
import info.gabrielszabo.jdi.data.ScanImpl;
import info.gabrielszabo.jdi.data.ScanInterface;
import info.gabrielszabo.jdi.data.TestInterface;
import info.gabrielszabo.jdi.data.TestInterfaceImpl1;
import info.gabrielszabo.jdi.data.TestInterfaceImpl2;
import info.gabrielszabo.jdi.data.TestInterfaceImpl3;
import info.gabrielszabo.jdi.data.TestInterfaceImpl4;
import info.gabrielszabo.jdi.data.TestInterfaceImpl5;
import info.gabrielszabo.jdi.data.TestInterfaceImpl6;
import info.gabrielszabo.jdi.domain.ServiceClassType;
import info.gabrielszabo.jdi.services.ServiceFactory;
import info.gabrielszabo.jdi.services.impl.ServiceFactoryImpl;

public class ServiceFactoryTest {
	
	private MockConfigService configService;
	private ServiceFactory serviceFactory;
	
	@BeforeEach
	void prepare() {
		configService = new MockConfigService();
		serviceFactory = new ServiceFactoryImpl(configService);
	}
	
	@Test
	void testNullClassParameter() {
		Optional<Object> ret = serviceFactory.getServiceImpl(null);
		
		assertFalse(ret.isPresent());
	}
	
	@Test
	void testCreateInstanceOfClassWithNoConstructor() {
		Optional<String> tmp = serviceFactory.getServiceImpl(String.class);
		
		assertTrue(tmp.isPresent());
		assertEquals("", tmp.get());
	}
	
	@Test
	void testCreateInstanceOfServiceImplWithNoConstructorSingleton() {
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
	void testCreateInstanceOfServiceImplWithNoConstructorMultiton() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl1.class.getName());
		configService.set(ServiceFactory.PREFIX_TYPE + TestInterface.class.getName(), "multiton");
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
		
		int hash1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotEquals(hash1, tmp.get().hashCode());
	}
	
	@Test
	void testCreateInstanceOfServiceWithConfigServiceAttribute() {
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
	void testCreateInstanceOfServiceImplWithMultipleAttributes() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl3.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + ParamInterface.class.getName(), ParamInterfaceImpl.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
	}
	
	@Test
	void testCreateInstanceOfServiceImplNotAvailableParams() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl4.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + ParamInterface.class.getName(), ParamInterfaceImpl.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
	}
	
	@Test
	void testMultipleConstructorParam() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl1.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + ParamInterface.class.getName(), ParamInterfaceImpl.class.getName());
		
		Optional<MultiParamConstructor> tmp = serviceFactory.getServiceImpl(MultiParamConstructor.class);
		
		assertTrue(tmp.isPresent());
		assertNotNull(tmp.get());
		assertNotNull(tmp.get().paramIntf);
		assertNotNull(tmp.get().paramTestIntf);
	}
	
	@Test
	void testDiscriminator() {
		configService.set("a." + ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl1.class.getName());
		configService.set("b." + ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl2.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl3.class.getName());
		configService.set(ServiceFactory.PREFIX_IMPL + ParamInterface.class.getName(), ParamInterfaceImpl.class.getName());
		configService.set(ServiceFactory.PREFIX_TYPE + TestInterface.class.getName(), ServiceClassType.MULTITON.toString());
		
		// Using discriminator a with no dot provided (e.g. "a").
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class, "a");
		assertTrue(tmp.isPresent());
		assertTrue(tmp.get() instanceof TestInterfaceImpl1);
		
		// Using discriminator b with provided dot (e.g. "b.").
		tmp = serviceFactory.getServiceImpl(TestInterface.class, "b.");
		assertTrue(tmp.isPresent());
		assertTrue(tmp.get() instanceof TestInterfaceImpl2);
		
		// Using fallback service lookup with no discriminator.
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.isPresent());
		assertTrue(tmp.get() instanceof TestInterfaceImpl3);
	}
	
	@Test
	void testEnum() {
		Optional<MyEnum> enumInstance = serviceFactory.getServiceImpl(MyEnum.class, "VALUE_A");
		assertTrue(enumInstance.isPresent());
		assertEquals(MyEnum.VALUE_A, enumInstance.get());
	}
	
	@Test
	void testEnumInjection() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl5.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.get() instanceof TestInterfaceImpl5);
		assertEquals(MyEnum.VALUE_B, ((TestInterfaceImpl5)tmp.get()).getEnum());
	}
	
	@Test
	void simpleClasspathScan() { 
		configService.setPackageToScan("info.gabrielszabo.jdi");
		
		Optional<ScanInterface> tmp = serviceFactory.getServiceImpl(ScanInterface.class);
		assertTrue(tmp.isPresent());
		assertTrue(tmp.get() instanceof ScanImpl);
	}
	
	@Test
	void testPrototypeAnnotation() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl6.class.getName());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.isPresent());
		
		int hashCode1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.isPresent());
		
		int hashCode2 = tmp.get().hashCode();
		
		assertFalse(hashCode1 == hashCode2);
	}
	
	@Test
	void overridePrototypeAnnotationByConfiguration() {
		configService.set(ServiceFactory.PREFIX_IMPL + TestInterface.class.getName(), TestInterfaceImpl6.class.getName());
		configService.set(ServiceFactory.PREFIX_TYPE + TestInterface.class.getName(), ServiceClassType.SINGLETON.toString());
		
		Optional<TestInterface> tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.isPresent());
		
		int hashCode1 = tmp.get().hashCode();
		
		tmp = serviceFactory.getServiceImpl(TestInterface.class);
		assertTrue(tmp.isPresent());
		
		int hashCode2 = tmp.get().hashCode();
		
		assertTrue(hashCode1 == hashCode2);
	}
	
}
