# Spring Boot JPA utils

Utilities for spring boot JPA on spring boot 3.

The minimum supported version is **Java 17**, since that's the required for Spring Boot 3.

This library gives you some utilities to perform performant operations with JPA, along with some repository addons that allow you to perform batch operations, or EntityManager operations.

## Things you can do with this library:

### Utility classes
- **EntityReflectionUtils** -> Reflection of entity objects to get all their fields, their table columns, their sequences, or their table names.
- **QueryBuilderUtils** -> Utility to create JPA or native sql queries.
- **SpecificationUtils** -> Contains some useful methods to easily create JPA Specification objects.

### Repository addons
- **SimpleRepositoryAddon** -> Allows you to perform EntityManager operations, along with some common operations.
- **BatchRepositoryAddon** -> Contains some useful methods to perform batch operations, along with JDBC batch operations.

## Installation
You just need to add the dependency to your pom:

``` xml
<dependency>
    <groupId>io.github.dokkaltek</groupId>
    <artifactId>spring-boot-jpa-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

