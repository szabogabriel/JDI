# JDI

JDI is a small dependency injection framework for Java. It uses DI via constructors. It expects a configuration service instance passed as well during creation, which is defined only as an interface and it is the hosting projects responsibility to implement it.

## Goal of the project

The project is aimed as a rather simplistic DI for Java. It was implemented using JDK 8 and consist of no external library dependency other then the Java SDK. The only exception to this are the unit tests, which were written using JUnit 4. The version integrated into Eclipse was used to develop the tests.

## Configuration via the configuration service

The configuration service is declared via the `ConfigService` interface and it contains only one method. No default implementation is provided. It is expected, that the implementation of the `ConfigService` provided, will always return an `Optional` instance (no null checks are performed).

The request values consist of two prefixed keys, the `type.` and `impl.` followed by the full path of the service interface or class to be instantiated. The value for the key prefixed with `impl.` must be the class, which should be used as a service implementation for the given service interface or class. The value for the key prefixed with `type.` is either `singleton` or `multiton`. Should there be no type value set, it defaults to `singleton`. A `singleton` service implementation would be cached inside the service factory and created only once. A `multiton` type service will be instantiated upon every request.

## Flowchart of the service factory

1. Determining the class to instantiate. Should the requested class be an interface or an abstract class, the class to be instantiated is loaded from the configuration service. Otherwise the requested class itself is returned as target for instantiation.

2. Should the instance of the given class be already cached, it can be returned directly. Otherwise a new instance is created, which is stored, if it is of a singleton type.

3. The creation is looking for constructors without any parameters. Shouldn't be any present, it sorts all the constructors by the number of parameters and tries to instantiate the given class be caching all the necessary parameters by using the service factory class. Should the `ConfigService` be requested, the instance provided to the service factory instance is used.

4. The given service implementation is returned as an `Optional`.
