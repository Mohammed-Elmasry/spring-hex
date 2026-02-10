# Spring-Hex Stub Files Reference

This document contains all stub files used by the spring-hex CLI tool for scaffolding hexagonal architecture with CQRS in Spring Boot projects.

---

## Table of Contents

1. [Domain Stubs](#domain-stubs)
2. [Infrastructure Stubs](#infrastructure-stubs)
3. [Mediator Stubs](#mediator-stubs)
4. [Placeholders Reference](#placeholders-reference)

---

## Domain Stubs

### entity.stub

**Location:** `src/main/resources/stubs/domain/entity.stub`

**Purpose:** Generates a domain entity representing core business concepts.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class {{ENTITY_NAME}} {
    
    private {{ENTITY_NAME}}Id id;
    
    // TODO: Add entity fields
}
```

---

### command.stub

**Location:** `src/main/resources/stubs/domain/command.stub`

**Purpose:** Generates a command DTO representing an intent to change state.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.command;

import {{BASE_PACKAGE}}.infrastructure.mediator.annotation.Command;
import lombok.Value;

@Command
@Value
public class {{COMMAND_NAME}} {
    // TODO: Add command fields
}
```

---

### command-handler.stub

**Location:** `src/main/resources/stubs/domain/command-handler.stub`

**Purpose:** Generates a command handler that processes commands and executes business logic.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.command;

import {{BASE_PACKAGE}}.infrastructure.mediator.CommandHandler;
import {{BASE_PACKAGE}}.infrastructure.mediator.annotation.CommandHandlerComponent;
import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.out.{{AGGREGATE_CAPITALIZED}}Repository;
import lombok.RequiredArgsConstructor;

@CommandHandlerComponent
@RequiredArgsConstructor
public class {{COMMAND_NAME}}Handler implements CommandHandler<{{COMMAND_NAME}}, Void> {
    
    private final {{AGGREGATE_CAPITALIZED}}Repository repository;
    
    @Override
    public Void handle({{COMMAND_NAME}} command) {
        // TODO: Implement command handling logic
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

---

### query.stub

**Location:** `src/main/resources/stubs/domain/query.stub`

**Purpose:** Generates a query DTO representing a request for data.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.query;

import {{BASE_PACKAGE}}.infrastructure.mediator.annotation.Query;
import lombok.Value;

@Query
@Value
public class {{QUERY_NAME}} {
    // TODO: Add query parameters
}
```

---

### query-handler.stub

**Location:** `src/main/resources/stubs/domain/query-handler.stub`

**Purpose:** Generates a query handler that retrieves and returns data.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.query;

import {{BASE_PACKAGE}}.infrastructure.mediator.QueryHandler;
import {{BASE_PACKAGE}}.infrastructure.mediator.annotation.QueryHandlerComponent;
import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.out.{{AGGREGATE_CAPITALIZED}}Repository;
import lombok.RequiredArgsConstructor;

@QueryHandlerComponent
@RequiredArgsConstructor
public class {{QUERY_NAME}}Handler implements QueryHandler<{{QUERY_NAME}}, {{RETURN_TYPE}}> {
    
    private final {{AGGREGATE_CAPITALIZED}}Repository repository;
    
    @Override
    public {{RETURN_TYPE}} handle({{QUERY_NAME}} query) {
        // TODO: Implement query handling logic
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

---

### service.stub

**Location:** `src/main/resources/stubs/domain/service.stub`

**Purpose:** Generates a domain service for complex business logic that doesn't fit in entities.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.service;

import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.out.{{AGGREGATE_CAPITALIZED}}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class {{AGGREGATE_CAPITALIZED}}Service {
    
    private final {{AGGREGATE_CAPITALIZED}}Repository repository;
    
    // TODO: Add domain service methods
}
```

---

### use-case-port.stub

**Location:** `src/main/resources/stubs/domain/use-case-port.stub`

**Purpose:** Generates an inbound port interface defining what the application does.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.in;

public interface {{USE_CASE_NAME}} {
    
    {{RETURN_TYPE}} execute({{PARAMETER_TYPE}} request);
}
```

---

### repository-port.stub

**Location:** `src/main/resources/stubs/domain/repository-port.stub`

**Purpose:** Generates an outbound port interface for persistence operations.

```java
package {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.out;

import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.model.{{ENTITY_NAME}};
import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.model.{{ENTITY_NAME}}Id;
import java.util.Optional;

public interface {{AGGREGATE_CAPITALIZED}}Repository {
    
    {{ENTITY_NAME}} save({{ENTITY_NAME}} entity);
    
    Optional<{{ENTITY_NAME}}> findById({{ENTITY_NAME}}Id id);
    
    void deleteById({{ENTITY_NAME}}Id id);
}
```

---

## Infrastructure Stubs

### controller.stub

**Location:** `src/main/resources/stubs/infrastructure/controller.stub`

**Purpose:** Generates a REST controller (inbound adapter) that receives HTTP requests.

```java
package {{BASE_PACKAGE}}.infrastructure.web.{{AGGREGATE}};

import {{BASE_PACKAGE}}.infrastructure.mediator.CommandBus;
import {{BASE_PACKAGE}}.infrastructure.mediator.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{{AGGREGATE_PLURAL}}")
@RequiredArgsConstructor
public class {{AGGREGATE_CAPITALIZED}}Controller {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    
    // TODO: Add controller endpoints
}
```

---

### jpa-repository.stub

**Location:** `src/main/resources/stubs/infrastructure/jpa-repository.stub`

**Purpose:** Generates a JPA repository adapter implementing the repository port.

```java
package {{BASE_PACKAGE}}.infrastructure.persistence.{{AGGREGATE}};

import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.model.{{ENTITY_NAME}};
import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.model.{{ENTITY_NAME}}Id;
import {{BASE_PACKAGE}}.domain.{{AGGREGATE}}.port.out.{{AGGREGATE_CAPITALIZED}}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class Jpa{{AGGREGATE_CAPITALIZED}}Repository implements {{AGGREGATE_CAPITALIZED}}Repository {
    
    private final {{AGGREGATE_CAPITALIZED}}JpaRepository jpaRepository;
    
    @Override
    public {{ENTITY_NAME}} save({{ENTITY_NAME}} entity) {
        // TODO: Map domain entity to JPA entity and save
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Optional<{{ENTITY_NAME}}> findById({{ENTITY_NAME}}Id id) {
        // TODO: Find JPA entity and map to domain entity
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public void deleteById({{ENTITY_NAME}}Id id) {
        // TODO: Delete JPA entity
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

---

### jpa-entity.stub

**Location:** `src/main/resources/stubs/infrastructure/jpa-entity.stub`

**Purpose:** Generates a JPA entity for database persistence (separate from domain entity).

```java
package {{BASE_PACKAGE}}.infrastructure.persistence.{{AGGREGATE}};

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "{{TABLE_NAME}}")
@Data
public class {{ENTITY_NAME}}JpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // TODO: Add JPA entity fields
}
```

---

## Mediator Stubs

### CommandBus.stub

**Location:** `src/main/resources/stubs/mediator/CommandBus.stub`

**Purpose:** Interface for dispatching commands to their handlers.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

public interface CommandBus {
    
    <R> R dispatch(Object command);
}
```

---

### SimpleCommandBus.stub

**Location:** `src/main/resources/stubs/mediator/SimpleCommandBus.stub`

**Purpose:** Implementation that auto-discovers command handlers via Spring context.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SimpleCommandBus implements CommandBus {
    
    private final ApplicationContext applicationContext;
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Object command) {
        Class<?> commandType = command.getClass();
        CommandHandler<Object, R> handler = findHandler(commandType);
        return handler.handle(command);
    }
    
    @SuppressWarnings("unchecked")
    private <R> CommandHandler<Object, R> findHandler(Class<?> commandType) {
        Map<String, CommandHandler> handlers = applicationContext.getBeansOfType(CommandHandler.class);
        
        for (CommandHandler<?, ?> handler : handlers.values()) {
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(
                handler.getClass(), CommandHandler.class
            );
            
            if (generics != null && generics[0].equals(commandType)) {
                return (CommandHandler<Object, R>) handler;
            }
        }
        
        throw new IllegalStateException("No handler found for command: " + commandType.getName());
    }
}
```

---

### QueryBus.stub

**Location:** `src/main/resources/stubs/mediator/QueryBus.stub`

**Purpose:** Interface for dispatching queries to their handlers.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

public interface QueryBus {
    
    <R> R dispatch(Object query);
}
```

---

### SimpleQueryBus.stub

**Location:** `src/main/resources/stubs/mediator/SimpleQueryBus.stub`

**Purpose:** Implementation that auto-discovers query handlers via Spring context.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SimpleQueryBus implements QueryBus {
    
    private final ApplicationContext applicationContext;
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Object query) {
        Class<?> queryType = query.getClass();
        QueryHandler<Object, R> handler = findHandler(queryType);
        return handler.handle(query);
    }
    
    @SuppressWarnings("unchecked")
    private <R> QueryHandler<Object, R> findHandler(Class<?> queryType) {
        Map<String, QueryHandler> handlers = applicationContext.getBeansOfType(QueryHandler.class);
        
        for (QueryHandler<?, ?> handler : handlers.values()) {
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(
                handler.getClass(), QueryHandler.class
            );
            
            if (generics != null && generics[0].equals(queryType)) {
                return (QueryHandler<Object, R>) handler;
            }
        }
        
        throw new IllegalStateException("No handler found for query: " + queryType.getName());
    }
}
```

---

### CommandHandler.stub

**Location:** `src/main/resources/stubs/mediator/CommandHandler.stub`

**Purpose:** Generic interface for all command handlers.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

public interface CommandHandler<C, R> {
    
    R handle(C command);
}
```

---

### QueryHandler.stub

**Location:** `src/main/resources/stubs/mediator/QueryHandler.stub`

**Purpose:** Generic interface for all query handlers.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator;

public interface QueryHandler<Q, R> {
    
    R handle(Q query);
}
```

---

### MediatorConfig.stub

**Location:** `src/main/resources/stubs/mediator/MediatorConfig.stub`

**Purpose:** Spring configuration class for mediator components.

```java
package {{BASE_PACKAGE}}.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "{{BASE_PACKAGE}}.infrastructure.mediator")
public class MediatorConfig {
}
```

---

### annotation/Command.stub

**Location:** `src/main/resources/stubs/mediator/annotation/Command.stub`

**Purpose:** Marker annotation for command DTOs.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
}
```

---

### annotation/Query.stub

**Location:** `src/main/resources/stubs/mediator/annotation/Query.stub`

**Purpose:** Marker annotation for query DTOs.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
}
```

---

### annotation/CommandHandlerComponent.stub

**Location:** `src/main/resources/stubs/mediator/annotation/CommandHandlerComponent.stub`

**Purpose:** Composite annotation marking a class as both a Spring component and command handler.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator.annotation;

import org.springframework.stereotype.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface CommandHandlerComponent {
}
```

---

### annotation/QueryHandlerComponent.stub

**Location:** `src/main/resources/stubs/mediator/annotation/QueryHandlerComponent.stub`

**Purpose:** Composite annotation marking a class as both a Spring component and query handler.

```java
package {{BASE_PACKAGE}}.infrastructure.mediator.annotation;

import org.springframework.stereotype.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface QueryHandlerComponent {
}
```

---

## Placeholders Reference

All stubs use placeholder tokens that are replaced during code generation:

| Placeholder | Description | Example |
|------------|-------------|---------|
| `{{BASE_PACKAGE}}` | Root package of the application | `com.myapp` |
| `{{AGGREGATE}}` | Aggregate name (lowercase) | `order` |
| `{{AGGREGATE_CAPITALIZED}}` | Aggregate name (capitalized) | `Order` |
| `{{AGGREGATE_PLURAL}}` | Aggregate name (plural, lowercase) | `orders` |
| `{{ENTITY_NAME}}` | Entity class name | `Order` |
| `{{COMMAND_NAME}}` | Command class name | `CreateOrderCommand` |
| `{{QUERY_NAME}}` | Query class name | `GetOrderByIdQuery` |
| `{{TABLE_NAME}}` | Database table name | `orders` |
| `{{RETURN_TYPE}}` | Return type for handlers/use cases | `OrderId`, `OrderDto` |
| `{{PARAMETER_TYPE}}` | Parameter type for use cases | `CreateOrderRequest` |
| `{{USE_CASE_NAME}}` | Use case interface name | `CreateOrderUseCase` |

---

## Summary

- **Total Stubs:** 24 files
- **Domain Stubs:** 8 files (entities, commands, queries, handlers, services, ports)
- **Infrastructure Stubs:** 3 files (controllers, JPA repositories, JPA entities)
- **Mediator Stubs:** 13 files (buses, handlers, annotations, config)

These stubs form the foundation of the spring-hex CLI tool, enabling rapid scaffolding of hexagonal architecture with CQRS patterns in Spring Boot projects.
