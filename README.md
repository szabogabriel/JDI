# JDI

JDI is a small dependency injection framework for Java. It uses DI via constructors. It expects a configuration service instance passed as well during creation, which is defined only as an interface and it is the hosting projects responsibility to implement it.

## Goal of the project

The project is aimed as a rather simplistic DI for Java. It was implemented using JDK 8 and consist of only a small number (1) of external library dependency other then the Java SDK for the runtime. The reason for the external library is so we would be able to run the unit tests, which were written using JUnit 5.

## Configuration via the configuration service

The configuration service is declared via the `ConfigService` interface and it contains only one method. No default implementation is provided. It is expected, that the implementation of the `ConfigService` provided, will always return an `Optional` instance (no null checks are performed).

The request values consist of two prefixed keys, the `type.` and `impl.` followed by the full path of the service interface or class to be instantiated. The value for the key prefixed with `impl.` must be the class, which should be used as a service implementation for the given service interface or class. The value for the key prefixed with `type.` is either `singleton` or `multiton`. Should there be no type value set, it defaults to `singleton`. A `singleton` service implementation would be cached inside the service factory and created only once. A `multiton` type service will be instantiated upon every request.

There is an option to extend the `impl` type configuration keys with a custom String prefix which than works as a descriminator. For instance having an interface `com.test.MyInterface`, we can declare configuration values like `impl.com.test.MyInterface` for the default implementation and additionally `MyCustomService.impl.com.test.MyInterface` for a custom implementation. Discriminators in the code can be set on a method parameter level by using the `@Discriminator` annotation.

The configuration service also has a default method for setting the root package for classpath scans. In its default value it returns an empty `Optional`, which is interpreted as there should be no classpath scan performed.

## Flowchart of the service factory

1. Determining the class to instantiate. Should the requested class be an interface or an abstract class, the class to be instantiated is loaded from the configuration service. Otherwise the requested class itself is returned as target for instantiation. If there is no entry in the configuration service for the interface or for the abstract class, then a classpath scan for subtypes is performed, if there was a root package defined in the configuration service.

2. Should the instance of the given class be already cached, it can be returned directly. Otherwise a new instance is created, which is stored, if it is of a singleton type.

3. The creation is looking for constructors without any parameters. Shouldn't be any present, it sorts all the constructors by the number of parameters and tries to instantiate the given class be caching all the necessary parameters by using the service factory class. Should the `ConfigService` be requested, the instance provided to the service factory instance is used.

4. The given service implementation is returned as an `Optional`.
