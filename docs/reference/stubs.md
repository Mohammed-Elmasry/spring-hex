---
title: Stubs
parent: Reference
nav_order: 3
---

# Stubs Reference

Stub files are template files used by Spring-Hex CLI to generate code. This reference documents all available stubs, their purpose, and the placeholders they support.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

Stubs are text files containing boilerplate code with placeholder variables. When you run a generation command, the CLI:

1. Loads the appropriate stub file
2. Replaces placeholders with actual values
3. Writes the result to your project

### Customizing Stubs

You can customize any stub by creating a `.hex/stubs/` directory in your project and copying the stub files you want to modify. Spring-Hex CLI will use your custom stubs instead of the defaults.

```bash
mkdir -p .hex/stubs/domain
# Copy and modify the command.stub file
cp /path/to/default/stubs/command.stub .hex/stubs/domain/command.stub
```

---

## Domain Stubs

Stubs for core domain layer components in hexagonal architecture.

### Model and Entity Stubs

#### model.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates a domain model class representing business entities.

**Used by:** `make:model`, `make:aggregate`

**Key Placeholders:**
- `{{BASE_PACKAGE}}` - Root application package
- `{{PACKAGE}}` - Target package for the model
- `{{AGGREGATE}}` - Lowercase aggregate name
- `{{AGGREGATE_CAPITALIZED}}` - Capitalized aggregate name
- `{{ENTITY_NAME}}` - Model class name

**Example Output:**
```java
package com.app.domain.order.model;

public class Order {
    private OrderId id;

    public Order() {
    }

    public OrderId getId() {
        return id;
    }

    public void setId(OrderId id) {
        this.id = id;
    }
}
```

---

#### entity.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates a domain entity (similar to model but with entity-specific characteristics).

**Used by:** `make:entity` (for domain entities, not JPA entities)

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Entity class name
- `{{PACKAGE}}` - Target package
- `{{AGGREGATE}}` - Aggregate name

---

#### aggregate.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates a domain aggregate class.

**Used by:** `make:aggregate`

**Key Placeholders:**
- `{{AGGREGATE_CAPITALIZED}}` - Aggregate class name
- `{{PACKAGE}}` - Target package

**Example Output:**
```java
package com.app.domain.order;

import com.app.domain.order.model.OrderId;

public class Order {
    private final OrderId id;

    public Order(OrderId id) {
        this.id = id;
    }

    public OrderId getId() {
        return id;
    }
}
```

---

#### aggregate-root.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates an aggregate root with enhanced capabilities (event sourcing, domain event support).

**Used by:** `make:aggregate` (advanced mode)

**Key Placeholders:**
- `{{AGGREGATE_CAPITALIZED}}` - Aggregate root class name
- `{{PACKAGE_DOMAIN_ROOT}}` - Domain root package

---

### CQRS Stubs

#### command.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a CQRS command class.

**Used by:** `make:command`

**Key Placeholders:**
- `{{COMMAND_NAME}}` - Command class name (e.g., CreateOrderCommand)
- `{{PACKAGE}}` - Command package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.command;

public class CreateOrderCommand {
    // Command properties
}
```

---

#### command-handler.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a command handler implementation.

**Used by:** `make:command`

**Key Placeholders:**
- `{{COMMAND_NAME}}` - Command class name
- `{{PACKAGE}}` - Handler package
- `{{AGGREGATE}}` - Aggregate name
- `{{PACKAGE_CQRS}}` - CQRS package for imports

**Example Output:**
```java
package com.app.domain.order.command;

public class CreateOrderCommandHandler implements CreateOrderCommandHandlerInterface {

    @Override
    public void handle(CreateOrderCommand command) {
        // Implementation
    }
}
```

---

#### command-handler-interface.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a command handler interface.

**Used by:** `make:command`

**Key Placeholders:**
- `{{COMMAND_NAME}}` - Command class name
- `{{PACKAGE}}` - Handler package

---

#### query.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a CQRS query class.

**Used by:** `make:query`

**Key Placeholders:**
- `{{QUERY_NAME}}` - Query class name (e.g., GetOrderByIdQuery)
- `{{PACKAGE}}` - Query package
- `{{AGGREGATE}}` - Aggregate name

---

#### query-handler.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a query handler implementation.

**Used by:** `make:query`

**Key Placeholders:**
- `{{QUERY_NAME}}` - Query class name
- `{{RETURN_TYPE}}` - Query return type
- `{{PACKAGE}}` - Handler package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.query;

public class GetOrderByIdQueryHandler implements GetOrderByIdQueryHandlerInterface {

    @Override
    public OrderDTO handle(GetOrderByIdQuery query) {
        // Implementation
        return null;
    }
}
```

---

#### query-handler-interface.stub
{: .d-inline-block }
CQRS
{: .label .label-blue }

Generates a query handler interface.

**Used by:** `make:query`

**Key Placeholders:**
- `{{QUERY_NAME}}` - Query class name
- `{{RETURN_TYPE}}` - Query return type
- `{{PACKAGE}}` - Handler package

---

### Event Stubs

#### domain-event.stub
{: .d-inline-block }
Events
{: .label .label-purple }

Generates a domain event class.

**Used by:** `make:event`

**Key Placeholders:**
- `{{EVENT_NAME}}` - Event class name (e.g., OrderCreatedEvent)
- `{{PACKAGE}}` - Event package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.event;

import java.time.Instant;

public class OrderCreatedEvent {
    private final Instant occurredOn;

    public OrderCreatedEvent() {
        this.occurredOn = Instant.now();
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
```

---

### Value Object Stubs

#### value-object.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates a value object class.

**Used by:** `make:value-object`

**Key Placeholders:**
- `{{VALUE_OBJECT_NAME}}` - Value object class name
- `{{PACKAGE}}` - Target package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.model;

import java.util.Objects;

public class Money {
    private final double amount;
    private final String currency;

    public Money(double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Double.compare(money.amount, amount) == 0 &&
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

---

#### value-object-id.stub
{: .d-inline-block }
Domain
{: .label .label-green }

Generates an ID value object (typically for aggregate IDs).

**Used by:** `make:aggregate`

**Key Placeholders:**
- `{{AGGREGATE_CAPITALIZED}}` - Aggregate name for ID class (e.g., OrderId)
- `{{PACKAGE}}` - Target package

---

### Port Stubs

#### input-port.stub
{: .d-inline-block }
Ports
{: .label .label-yellow }

Generates an input port interface (application service interface).

**Used by:** `make:port --in`

**Key Placeholders:**
- `{{PORT_NAME}}` - Port interface name
- `{{PACKAGE_PORT_IN}}` - Input port package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.port.in;

public interface CreateOrderUseCase {
    void execute(CreateOrderCommand command);
}
```

---

#### output-port.stub
{: .d-inline-block }
Ports
{: .label .label-yellow }

Generates an output port interface (typically for repositories or external services).

**Used by:** `make:port` (default)

**Key Placeholders:**
- `{{PORT_NAME}}` - Port interface name
- `{{PACKAGE_PORT_OUT}}` - Output port package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.domain.order.port.out;

import com.app.domain.order.model.Order;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
}
```

---

#### use-case-port.stub
{: .d-inline-block }
Ports
{: .label .label-yellow }

Generates a use case port interface.

**Used by:** Advanced port generation scenarios

**Key Placeholders:**
- `{{PORT_NAME}}` - Use case interface name
- `{{PACKAGE_PORT_IN}}` - Input port package

---

#### repository-port.stub
{: .d-inline-block }
Ports
{: .label .label-yellow }

Generates a repository port interface with standard CRUD operations.

**Used by:** `make:repository`

**Key Placeholders:**
- `{{PORT_NAME}}` - Repository interface name
- `{{ENTITY_NAME}}` - Entity type
- `{{PACKAGE_PORT_OUT}}` - Output port package

---

### DTO Stubs

#### request.stub
{: .d-inline-block }
DTOs
{: .label .label-blue }

Generates a request DTO for API endpoints.

**Used by:** `make:request`

**Key Placeholders:**
- `{{REQUEST_NAME}}` - Request class name (e.g., CreateOrderRequest)
- `{{PACKAGE}}` - DTO package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.infrastructure.order.controller.dto;

import javax.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
```

---

#### response.stub
{: .d-inline-block }
DTOs
{: .label .label-blue }

Generates a response DTO for API endpoints.

**Used by:** `make:response`

**Key Placeholders:**
- `{{RESPONSE_NAME}}` - Response class name (e.g., OrderDetailsResponse)
- `{{PACKAGE}}` - DTO package
- `{{AGGREGATE}}` - Aggregate name

---

## Infrastructure Stubs

Stubs for infrastructure layer components.

### Controller Stubs

#### controller.stub
{: .d-inline-block }
Infrastructure
{: .label .label-red }

Generates a REST controller.

**Used by:** `make:controller`, `make:crud`

**Key Placeholders:**
- `{{AGGREGATE_CAPITALIZED}}` - Controller class prefix (e.g., OrderController)
- `{{PACKAGE}}` - Controller package
- `{{AGGREGATE}}` - Aggregate name (for URL mapping)
- `{{PACKAGE_MODEL}}` - Model package for imports

**Example Output:**
```java
package com.app.infrastructure.order.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok().build();
    }
}
```

---

### Persistence Stubs

#### jpa-entity.stub
{: .d-inline-block }
JPA
{: .label .label-green }

Generates a JPA entity class.

**Used by:** `make:entity`, `make:crud`

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Entity class name
- `{{TABLE_NAME}}` - Database table name
- `{{PACKAGE}}` - Entity package

**Example Output:**
```java
package com.app.infrastructure.order.persistence;

import javax.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
```

---

#### jpa-repository.stub
{: .d-inline-block }
JPA
{: .label .label-green }

Generates a Spring Data JPA repository interface.

**Used by:** `make:repository` (with JPA), `make:crud`

**Aliases:** `spring-data-repository.stub`

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Entity class name
- `{{PACKAGE}}` - Repository package

**Example Output:**
```java
package com.app.infrastructure.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}
```

---

#### mongo-document.stub
{: .d-inline-block }
MongoDB
{: .label .label-green }

Generates a MongoDB document entity.

**Used by:** `make:entity` (with MongoDB store)

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Document class name
- `{{TABLE_NAME}}` - Collection name
- `{{PACKAGE}}` - Document package

**Example Output:**
```java
package com.app.infrastructure.order.persistence;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "orders")
public class OrderDocument {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
```

---

#### spring-data-mongo-repository.stub
{: .d-inline-block }
MongoDB
{: .label .label-green }

Generates a Spring Data MongoDB repository interface.

**Used by:** `make:repository -s mongodb`

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Document class name
- `{{PACKAGE}}` - Repository package

---

#### redis-hash-entity.stub
{: .d-inline-block }
Redis
{: .label .label-red }

Generates a Redis hash entity.

**Used by:** `make:entity` (with Redis store)

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Hash class name
- `{{TABLE_NAME}}` - Redis key prefix
- `{{PACKAGE}}` - Entity package

**Example Output:**
```java
package com.app.infrastructure.session.persistence;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;

@RedisHash("sessions")
public class SessionHash {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
```

---

#### redis-repository.stub
{: .d-inline-block }
Redis
{: .label .label-red }

Generates a Spring Data Redis repository interface.

**Used by:** `make:repository -s redis`

---

### Adapter Stubs

#### adapter.stub
{: .d-inline-block }
Infrastructure
{: .label .label-red }

Generates a generic adapter implementing a port.

**Used by:** `make:adapter`

**Key Placeholders:**
- `{{ADAPTER_NAME}}` - Adapter class name
- `{{PORT_NAME}}` - Port interface name
- `{{PACKAGE}}` - Adapter package
- `{{PACKAGE_PORT_OUT}}` - Port package for imports

**Example Output:**
```java
package com.app.infrastructure.order.adapter;

import com.app.domain.order.port.out.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaOrderRepositoryAdapter implements OrderRepository {

    @Override
    public Optional<Order> findById(OrderId id) {
        // Implementation
        return Optional.empty();
    }
}
```

---

#### repository-adapter.stub
{: .d-inline-block }
Infrastructure
{: .label .label-red }

Generates a repository adapter with Spring Data integration.

**Used by:** `make:repository`

**Key Placeholders:**
- `{{ADAPTER_NAME}}` - Adapter class name
- `{{ENTITY_NAME}}` - Entity/domain model name
- `{{PORT_NAME}}` - Repository port interface
- `{{PACKAGE}}` - Adapter package

---

#### mongo-repository-adapter.stub
{: .d-inline-block }
MongoDB
{: .label .label-green }

Generates a MongoDB-specific repository adapter.

**Used by:** `make:repository -s mongodb`

---

#### redis-repository-adapter.stub
{: .d-inline-block }
Redis
{: .label .label-red }

Generates a Redis-specific repository adapter.

**Used by:** `make:repository -s redis`

---

#### event-listener.stub
{: .d-inline-block }
Events
{: .label .label-purple }

Generates a domain event listener.

**Used by:** `make:event`

**Key Placeholders:**
- `{{EVENT_NAME}}` - Event class name
- `{{PACKAGE}}` - Listener package
- `{{PACKAGE_EVENT}}` - Event package for imports

**Example Output:**
```java
package com.app.infrastructure.order.event;

import com.app.domain.order.event.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventListener {

    @EventListener
    public void handle(OrderCreatedEvent event) {
        // Handle event
    }
}
```

---

### Mapper Stubs

#### mapper.stub
{: .d-inline-block }
Infrastructure
{: .label .label-red }

Generates a mapper for converting between domain models and persistence entities.

**Used by:** `make:mapper`, `make:crud`

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Entity name
- `{{PACKAGE}}` - Mapper package
- `{{PACKAGE_MODEL}}` - Domain model package
- `{{AGGREGATE}}` - Aggregate name

**Example Output:**
```java
package com.app.infrastructure.order.mapper;

import com.app.domain.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderEntity toEntity(Order domain) {
        if (domain == null) return null;

        OrderEntity entity = new OrderEntity();
        // Mapping logic
        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        if (entity == null) return null;

        Order domain = new Order();
        // Mapping logic
        return domain;
    }
}
```

---

### Configuration Stubs

#### domain-config.stub
{: .d-inline-block }
Configuration
{: .label .label-blue }

Generates a Spring configuration class for domain beans.

**Used by:** `make:module`

**Key Placeholders:**
- `{{AGGREGATE_CAPITALIZED}}` - Configuration class name
- `{{PACKAGE}}` - Config package

---

#### bean-method-handler.stub
{: .d-inline-block }
Configuration
{: .label .label-blue }

Generates a bean method for registering command/query handlers.

**Used by:** Internal configuration generation

---

#### bean-method-handler-events.stub
{: .d-inline-block }
Configuration
{: .label .label-blue }

Generates a bean method for event handlers.

**Used by:** Internal configuration generation

---

#### bean-method-service.stub
{: .d-inline-block }
Configuration
{: .label .label-blue }

Generates a bean method for services.

**Used by:** `make:crud` (service configuration)

---

## Mediator Stubs

Stubs for CQRS mediator infrastructure.

#### CommandBus.stub
{: .d-inline-block }
Mediator
{: .label .label-purple }

Generates the command bus interface.

**Used by:** `make:mediator`

**Example Output:**
```java
package com.app.application.mediator;

public interface CommandBus {
    <C> void dispatch(C command);
}
```

---

#### SimpleCommandBus.stub
{: .d-inline-block }
Mediator
{: .label .label-purple }

Generates the simple command bus implementation.

**Used by:** `make:mediator`

---

#### QueryBus.stub
{: .d-inline-block }
Mediator
{: .label .label-purple }

Generates the query bus interface.

**Used by:** `make:mediator`

**Example Output:**
```java
package com.app.application.mediator;

public interface QueryBus {
    <Q, R> R dispatch(Q query);
}
```

---

#### SimpleQueryBus.stub
{: .d-inline-block }
Mediator
{: .label .label-purple }

Generates the simple query bus implementation.

**Used by:** `make:mediator`

---

#### MediatorConfig.stub
{: .d-inline-block }
Mediator
{: .label .label-purple }

Generates Spring configuration for mediator beans.

**Used by:** `make:mediator`

---

## MVC Stubs

Stubs for traditional MVC/CRUD architecture.

These stubs are simplified versions of their hexagonal counterparts, designed for rapid CRUD development:

- **model.stub** - Simple entity model without domain logic
- **entity.stub** - JPA entity for CRUD operations
- **repository.stub** - Spring Data repository interface
- **mapper.stub** - Simple DTO mapper
- **service.stub** - Service layer with CRUD operations
- **controller.stub** - REST controller with CRUD endpoints

**Used by:** `make:crud`

---

## Migration Stubs

Stubs for database migration files.

### Flyway Stubs

#### flyway-sql.stub
{: .d-inline-block }
Flyway
{: .label .label-green }

Generates a Flyway SQL migration file.

**Used by:** `make:migration` (default)

**Key Placeholders:**
- `{{MIGRATION_NAME}}` - Migration description
- `{{TIMESTAMP}}` - Timestamp for version

**Example Output:**
```sql
-- Migration: create_orders_table
-- Created: 2026-02-10 12:30:45

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**File naming:** `V20260210123045__create_orders_table.sql`

---

#### flyway-revert-sql.stub
{: .d-inline-block }
Flyway
{: .label .label-green }

Generates a Flyway undo migration file (for Teams/Pro).

**Used by:** `make:migration` (with undo support)

**Example Output:**
```sql
-- Undo Migration: create_orders_table

DROP TABLE IF EXISTS orders;
```

**File naming:** `U20260210123045__create_orders_table.sql`

---

### Liquibase Stubs

#### liquibase-changeset-sql.stub
{: .d-inline-block }
Liquibase
{: .label .label-blue }

Generates a Liquibase SQL changeset.

**Used by:** `make:migration --liquibase --format sql`

**Key Placeholders:**
- `{{CHANGESET_ID}}` - Unique changeset identifier
- `{{MIGRATION_NAME}}` - Migration description

**Example Output:**
```sql
--liquibase formatted sql

--changeset author:create_orders_table-1
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### liquibase-changeset-xml.stub
{: .d-inline-block }
Liquibase
{: .label .label-blue }

Generates a Liquibase XML changeset.

**Used by:** `make:migration --liquibase --format xml`

**Example Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd">

    <changeSet id="create_orders_table-1" author="spring-hex">
        <createTable tableName="orders">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

---

#### liquibase-changeset-yaml.stub
{: .d-inline-block }
Liquibase
{: .label .label-blue }

Generates a Liquibase YAML changeset.

**Used by:** `make:migration --liquibase --format yaml`

**Example Output:**
```yaml
databaseChangeLog:
  - changeSet:
      id: create_orders_table-1
      author: spring-hex
      changes:
        - createTable:
            tableName: orders
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: created_at
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
```

---

#### liquibase-master-xml.stub
{: .d-inline-block }
Liquibase
{: .label .label-blue }

Generates a master XML changelog file.

**Used by:** `make:migration --liquibase` (initial setup)

**Example Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd">

    <include file="db/changelog/changelog-20260210-create-orders.xml"/>
</databaseChangeLog>
```

---

#### liquibase-master-yaml.stub
{: .d-inline-block }
Liquibase
{: .label .label-blue }

Generates a master YAML changelog file.

**Used by:** `make:migration --liquibase --format yaml` (initial setup)

---

## Data Seeding Stubs

Stubs for data factories, seeders, and seeding infrastructure.

#### factory.stub
{: .d-inline-block }
Data
{: .label .label-purple }

Generates a data factory class using Datafaker for creating fake entity instances.

**Used by:** `make:factory`

**Key Placeholders:**
- `{{ENTITY_NAME}}` - Entity class name
- `{{PACKAGE_MODEL}}` - Resolved model package for entity import

**Example Output:**
```java
package com.app.infrastructure.factory.user;

import com.app.domain.user.model.User;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public class UserFactory {

    private static final Faker faker = new Faker();

    public static User create() {
        return User.builder()
                // TODO: Set fields using faker
                .build();
    }

    public static List<User> create(int count) {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(create());
        }
        return list;
    }
}
```

---

#### seeder.stub
{: .d-inline-block }
Data
{: .label .label-purple }

Generates a database seeder component that implements the `Seeder` interface.

**Used by:** `make:seeder`

**Key Placeholders:**
- `{{SEEDER_NAME}}` - Seeder class name
- `{{ENTITY_NAME}}` - Entity class name for repository/factory imports
- `{{PACKAGE_FACTORY}}` - Resolved factory package
- `{{PACKAGE_REPOSITORY}}` - Resolved persistence/repository package

**Example Output:**
```java
package com.app.infrastructure.seeder;

import com.app.infrastructure.factory.user.UserFactory;
import com.app.infrastructure.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements Seeder {

    private final UserRepository repository;

    @Override
    public void seed() {
        log.info("UserSeeder: seeding...");
        // Define your seed data here
        log.info("UserSeeder: done");
    }
}
```

---

#### seeder-interface.stub
{: .d-inline-block }
Data
{: .label .label-purple }

Generates the `Seeder` interface that all seeders implement. Auto-generated once on the first `make:seeder` call.

**Used by:** `make:seeder` (auto-generated)

**Example Output:**
```java
package com.app.infrastructure.seeder;

public interface Seeder {

    void seed();
}
```

---

#### seed-runner.stub
{: .d-inline-block }
Data
{: .label .label-purple }

Generates the `SeedRunner` component that dispatches `--seed=` arguments to the appropriate seeders. Auto-generated once on the first `make:seeder` call.

**Used by:** `make:seeder` (auto-generated)

**Key Features:**
- `db:seed --all` runs seeders in the order listed in `SEEDERS`
- `db:seed <SeederName>` runs a single seeder by bean name
- Developers control execution order by arranging the `SEEDERS` list

**Example Output:**
```java
package com.app.infrastructure.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedRunner implements CommandLineRunner {

    private final ApplicationContext context;

    // Add your seeders here in the order they should run.
    // Independent entities first, then entities that depend on them.
    private static final List<Class<? extends Seeder>> SEEDERS = List.of(
            // AuthorSeeder.class,
            // BookSeeder.class
    );

    @Override
    public void run(String... args) throws Exception {
        // Dispatches --seed=all or --seed=<SeederName>
    }
}
```

---

## Test Stubs

Stubs for test class generation.

#### unit-test.stub
{: .d-inline-block }
Testing
{: .label .label-yellow }

Generates a JUnit unit test class.

**Used by:** `make:test --unit`

**Key Placeholders:**
- `{{TEST_NAME}}` - Test class name
- `{{TEST_PACKAGE}}` - Test package

**Example Output:**
```java
package com.app.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    void testExample() {
        // Test implementation
    }
}
```

---

#### feature-test.stub
{: .d-inline-block }
Testing
{: .label .label-yellow }

Generates a Spring Boot integration/feature test.

**Used by:** `make:test` (default)

**Example Output:**
```java
package com.app.infrastructure.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEndpoint() throws Exception {
        // Test implementation
    }
}
```

---

## Placeholder Reference

Complete reference of all placeholder variables used in stubs.

| Placeholder | Description | Used In | Example Value |
|-------------|-------------|---------|---------------|
| `{{BASE_PACKAGE}}` | Root application package | All stubs | `com.mycompany.app` |
| `{{PACKAGE}}` | Target package for generated file | All stubs | `com.mycompany.app.domain.order.command` |
| `{{AGGREGATE}}` | Lowercase aggregate name | Hexagonal stubs | `order` |
| `{{AGGREGATE_CAPITALIZED}}` | Capitalized aggregate name | Hexagonal stubs | `Order` |
| `{{ENTITY_NAME}}` | Entity class name | Entity/Model stubs | `OrderEntity` |
| `{{COMMAND_NAME}}` | Command class name | Command stubs | `CreateOrderCommand` |
| `{{QUERY_NAME}}` | Query class name | Query stubs | `GetOrderByIdQuery` |
| `{{EVENT_NAME}}` | Event class name | Event stubs | `OrderCreatedEvent` |
| `{{TABLE_NAME}}` | Database table name | Entity/Migration stubs | `orders` |
| `{{PORT_NAME}}` | Port interface name | Port/Adapter stubs | `OrderRepository` |
| `{{ADAPTER_NAME}}` | Adapter class name | Adapter stubs | `JpaOrderRepositoryAdapter` |
| `{{VALUE_OBJECT_NAME}}` | Value object name | Value object stubs | `Money` |
| `{{REQUEST_NAME}}` | Request DTO name | Request stubs | `CreateOrderRequest` |
| `{{RESPONSE_NAME}}` | Response DTO name | Response stubs | `OrderDetailsResponse` |
| `{{RETURN_TYPE}}` | Query handler return type | Query handler stubs | `OrderDTO` |
| `{{PACKAGE_MODEL}}` | Resolved model package | All hexagonal stubs | `com.app.domain.order.model` |
| `{{PACKAGE_PORT_IN}}` | Resolved input port package | Port/Use case stubs | `com.app.domain.order.port.in` |
| `{{PACKAGE_PORT_OUT}}` | Resolved output port package | Port/Adapter stubs | `com.app.domain.order.port.out` |
| `{{PACKAGE_CQRS}}` | Resolved CQRS package | Command/Query stubs | `com.app.domain.order.command` |
| `{{PACKAGE_MEDIATOR}}` | Resolved mediator package | Mediator stubs | `com.app.application.mediator` |
| `{{PACKAGE_EVENT}}` | Resolved event package | Event stubs | `com.app.domain.order.event` |
| `{{PACKAGE_DOMAIN_ROOT}}` | Resolved domain root package | Aggregate stubs | `com.app.domain.order` |
| `{{TEST_PACKAGE}}` | Test package | Test stubs | `com.app.domain.order` |
| `{{TEST_NAME}}` | Test class name | Test stubs | `OrderServiceTest` |
| `{{SEEDER_NAME}}` | Seeder class name | Seeder stubs | `UserSeeder` |
| `{{PACKAGE_FACTORY}}` | Resolved factory package | Seeder stubs | `com.app.infrastructure.factory.user` |
| `{{PACKAGE_REPOSITORY}}` | Resolved repository package | Seeder stubs | `com.app.infrastructure.persistence.user` |
| `{{MIGRATION_NAME}}` | Migration name | Migration stubs | `create_orders_table` |
| `{{CHANGESET_ID}}` | Liquibase changeset ID | Liquibase stubs | `create_orders_table-1` |
| `{{TIMESTAMP}}` | Current timestamp | Flyway stubs | `20260210123045` |

---

## Customization Guide

### Creating Custom Stubs

1. Create the stubs directory:
```bash
mkdir -p .hex/stubs/domain
```

2. Copy the default stub you want to customize:
```bash
# Assuming default stubs are in the tool installation
cp default-stubs/command.stub .hex/stubs/domain/command.stub
```

3. Edit the stub with your customizations:
```java
package {{PACKAGE}};

import {{BASE_PACKAGE}}.common.Command;
import lombok.Data;

/**
 * Command: {{COMMAND_NAME}}
 * Aggregate: {{AGGREGATE_CAPITALIZED}}
 */
@Data
public class {{COMMAND_NAME}} implements Command {
    // Your custom template
}
```

4. The CLI will now use your custom stub for all command generation.

### Best Practices

1. **Keep placeholders consistent** - Use the documented placeholder names
2. **Add comments** - Include helpful comments in your stubs
3. **Import common dependencies** - Pre-populate common imports (Lombok, validation, etc.)
4. **Follow project conventions** - Match your team's coding style
5. **Version control stubs** - Commit `.hex/stubs/` to your repository
6. **Document customizations** - Note why you customized specific stubs

### Testing Custom Stubs

Generate code with the `--output` flag to test stubs without affecting your main codebase:

```bash
spring-hex make:command TestCommand -a test -o /tmp/test-output
```

Review the generated file to ensure your customizations work as expected.
