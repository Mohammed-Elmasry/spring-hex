---
title: Commands
parent: Reference
nav_order: 1
---

# Commands Reference

Complete reference for all Spring-Hex CLI commands. Commands are organized by category for easier navigation.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Setup Commands

### init
{: .d-inline-block }
Setup
{: .label .label-blue }

Initialize a `.hex/config.yml` configuration file in the current project.

**Usage:**
```bash
spring-hex init [options]
```

| Option | Description |
|--------|-------------|
| `--force` | Overwrite existing configuration file |
| `-p, --package` | Specify base package (auto-detected if not provided) |
| `-o, --output` | Output directory for config file (defaults to current directory) |

**Example:**
```bash
spring-hex init
spring-hex init --force -p com.mycompany.app
```

**Generated File:**
Creates `.hex/config.yml` with default path configurations for both hexagonal and CRUD architectures.

---

## Hexagonal Architecture Commands

### make:mediator
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate the mediator infrastructure for CQRS command and query buses.

**Usage:**
```bash
spring-hex make:mediator [options]
```

| Option | Description |
|--------|-------------|
| `-p, --package` | Base package (auto-detected if not specified) |
| `-o, --output` | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:mediator
```

**Generated Files:**
- `CommandBus` interface
- `SimpleCommandBus` implementation
- `QueryBus` interface
- `SimpleQueryBus` implementation
- `MediatorConfig` Spring configuration class

---

### make:module
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a complete hexagonal module with domain, application, and infrastructure layers.

**Usage:**
```bash
spring-hex make:module <moduleName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<moduleName>` | Yes | Name of the module/aggregate (e.g., order, user) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:module order
```

**Generated Structure:**
Creates a complete package structure with domain, application ports, and infrastructure directories ready for development.

---

### make:command
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a CQRS command and its handler.

**Usage:**
```bash
spring-hex make:command <commandName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<commandName>` | Yes | Name of the command (e.g., CreateOrder) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--no-handler` | No | Skip generating the command handler |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:command CreateOrder -a order
spring-hex make:command UpdateOrderStatus -a order --no-handler
```

**Generated Files:**
- Command class (e.g., `CreateOrderCommand`)
- Command handler class (e.g., `CreateOrderCommandHandler`) - unless `--no-handler` is specified
- Command handler interface (e.g., `CreateOrderCommandHandlerInterface`)

---

### make:query
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a CQRS query and its handler.

**Usage:**
```bash
spring-hex make:query <queryName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<queryName>` | Yes | Name of the query (e.g., GetOrderById) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--no-handler` | No | Skip generating the query handler |
| `-r, --return-type` | No | Return type for the query handler (defaults to void) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:query GetOrderById -a order -r OrderDTO
spring-hex make:query ListOrders -a order --return-type "List<OrderSummary>"
```

**Generated Files:**
- Query class (e.g., `GetOrderByIdQuery`)
- Query handler class (e.g., `GetOrderByIdQueryHandler`) - unless `--no-handler` is specified
- Query handler interface (e.g., `GetOrderByIdQueryHandlerInterface`)

---

### make:model
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a domain model class.

**Usage:**
```bash
spring-hex make:model <modelName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<modelName>` | Yes | Name of the model (e.g., Order, Customer) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:model Order -a order
spring-hex make:model OrderItem -a order
```

**Generated Files:**
- Domain model class in the appropriate aggregate package

---

### make:entity
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a JPA entity for persistence.

**Usage:**
```bash
spring-hex make:entity <entityName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<entityName>` | Yes | Name of the entity (e.g., OrderEntity) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--table` | No | Database table name (defaults to snake_case of entity name) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:entity OrderEntity -a order
spring-hex make:entity OrderEntity -a order --table orders
```

**Generated Files:**
- JPA entity class with `@Entity` and `@Table` annotations

---

### make:aggregate
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate an aggregate root with ID value object.

**Usage:**
```bash
spring-hex make:aggregate <aggregateName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<aggregateName>` | Yes | Name of the aggregate (e.g., Order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:aggregate Order
```

**Generated Files:**
- Aggregate root class
- ID value object (e.g., `OrderId`)

---

### make:value-object
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a domain value object.

**Usage:**
```bash
spring-hex make:value-object <name> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<name>` | Yes | Name of the value object (e.g., Money, Email) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:value-object Money -a order
spring-hex make:value-object OrderStatus -a order
```

**Generated Files:**
- Immutable value object class with equality based on value

---

### make:event
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a domain event and its listener.

**Usage:**
```bash
spring-hex make:event <eventName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<eventName>` | Yes | Name of the event (e.g., OrderCreated - "Event" suffix auto-appended) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--no-listener` | No | Skip generating the event listener |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:event OrderCreated -a order
spring-hex make:event OrderStatusChanged -a order --no-listener
```

**Generated Files:**
- Domain event class (e.g., `OrderCreatedEvent`)
- Event listener class (e.g., `OrderCreatedEventListener`) - unless `--no-listener` is specified

---

### make:port
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a port interface (input or output).

**Usage:**
```bash
spring-hex make:port <portName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<portName>` | Yes | Name of the port interface (e.g., OrderRepository) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--in` | No | Generate as input port (default is output port) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:port OrderRepository -a order
spring-hex make:port CreateOrderUseCase -a order --in
```

**Generated Files:**
- Port interface in the appropriate port package (input or output)

---

### make:adapter
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate an adapter implementing a port.

**Usage:**
```bash
spring-hex make:adapter <adapterName> -a <aggregate> --port <portName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<adapterName>` | Yes | Name of the adapter (e.g., JpaOrderRepository) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `--port` | Yes | Port interface name that this adapter implements |
| `--category` | No | Adapter category subdirectory (e.g., persistence, messaging) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:adapter JpaOrderRepository -a order --port OrderRepository
spring-hex make:adapter JpaOrderRepository -a order --port OrderRepository --category persistence
```

**Generated Files:**
- Adapter class implementing the specified port interface

---

### make:repository
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a repository port and adapter with Spring Data support.

**Usage:**
```bash
spring-hex make:repository <entityName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<entityName>` | Yes | Name of the entity (e.g., Order) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-s, --store` | No | Data store type: `jpa` (default), `mongodb`, or `redis` |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:repository Order -a order
spring-hex make:repository Order -a order -s mongodb
spring-hex make:repository Session -a auth -s redis
```

**Generated Files:**
- Repository port interface
- Repository adapter implementation
- Spring Data repository interface (JPA/MongoDB/Redis specific)

---

### make:mapper
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a mapper for converting between domain models and persistence entities.

**Usage:**
```bash
spring-hex make:mapper <entityName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<entityName>` | Yes | Name of the entity (e.g., Order) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:mapper Order -a order
```

**Generated Files:**
- Mapper class with methods to convert between domain model and entity

---

### make:controller
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a REST controller for an aggregate.

**Usage:**
```bash
spring-hex make:controller <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<aggregate>` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:controller order
```

**Generated Files:**
- REST controller class with `@RestController` and basic endpoint structure

---

### make:request
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a request DTO for API endpoints.

**Usage:**
```bash
spring-hex make:request <requestName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<requestName>` | Yes | Name of the request (e.g., CreateOrder - "Request" suffix auto-appended) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:request CreateOrder -a order
spring-hex make:request UpdateOrderStatus -a order
```

**Generated Files:**
- Request DTO class (e.g., `CreateOrderRequest`) with validation annotations

---

### make:response
{: .d-inline-block }
Hexagonal
{: .label .label-green }

Generate a response DTO for API endpoints.

**Usage:**
```bash
spring-hex make:response <responseName> -a <aggregate> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<responseName>` | Yes | Name of the response (e.g., OrderDetails - "Response" suffix auto-appended) |
| `-a, --aggregate` | Yes | Aggregate name (e.g., order) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:response OrderDetails -a order
spring-hex make:response OrderSummary -a order
```

**Generated Files:**
- Response DTO class (e.g., `OrderDetailsResponse`)

---

## Data Seeding Commands

### make:factory
{: .d-inline-block }
Data
{: .label .label-green }

Generate a data factory class using Datafaker for creating fake entity instances.

**Usage:**
```bash
spring-hex make:factory <entityName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<entityName>` | Yes | Entity name (e.g., User, Product) |
| `-a, --aggregate` | No | Aggregate name (defaults to entity name lowercase) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:factory User
spring-hex make:factory OrderItem -a order
```

**Generated Files:**
- Factory class with static `create()` and `create(int count)` methods
- Uses [Datafaker](https://www.datafaker.net/) for realistic fake data

**Usage in Code:**
```java
// Single entity
User user = UserFactory.create();

// Multiple entities
List<User> users = UserFactory.create(50);

// Nested factories for aggregate composition
Order order = Order.builder()
        .customer(CustomerFactory.create())
        .items(OrderItemFactory.create(3))
        .build();
```

**Note:** Add `net.datafaker:datafaker` to your project dependencies to use factories.

---

### make:seeder
{: .d-inline-block }
Data
{: .label .label-green }

Generate a database seeder class for populating development and test data.

**Usage:**
```bash
spring-hex make:seeder <seederName> --entity <entityName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<seederName>` | Yes | Seeder name (e.g., UserSeeder — "Seeder" suffix auto-appended) |
| `--entity` | Yes | Entity name for factory/repository imports (e.g., User) |
| `-a, --aggregate` | No | Aggregate name (defaults to entity name lowercase) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:seeder UserSeeder --entity User
spring-hex make:seeder OrderItemSeeder --entity OrderItem -a order
```

**Generated Files:**
- Seeder class implementing `Seeder` interface with a `seed()` method
- `Seeder` interface (auto-generated once, on first seeder creation)
- `SeedRunner` infrastructure component (auto-generated once, on first seeder creation)

**Execution Order:**

The `SeedRunner` contains an ordered list of seeder classes. When running `db:seed --all`, seeders execute in the order they appear in the list. Add your seeders in dependency order — independent entities first:

```java
private static final List<Class<? extends Seeder>> SEEDERS = List.of(
        AuthorSeeder.class,    // no dependencies — runs first
        BookSeeder.class       // depends on Author — runs second
);
```

---

### db:seed
{: .d-inline-block }
Data
{: .label .label-green }

Run database seeders via the project's build tool.

**Usage:**
```bash
spring-hex db:seed <seederName>
spring-hex db:seed --all
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<seederName>` | No | Seeder class name to run (e.g., UserSeeder) |
| `--all` | No | Run all seeders |

**Example:**
```bash
spring-hex db:seed UserSeeder
spring-hex db:seed --all
```

**Behavior:**
- Detects Maven or Gradle and runs the Spring Boot application with `--seed=<target>` argument
- The auto-generated `SeedRunner` picks up the argument and invokes the matching seeder's `seed()` method
- `--all` runs all seeders listed in `SeedRunner.SEEDERS` in the order they appear

---

## CRUD Commands

### make:crud
{: .d-inline-block }
CRUD
{: .label .label-yellow }

Generate a complete CRUD implementation with model, repository, service, and controller.

**Usage:**
```bash
spring-hex make:crud <entityName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<entityName>` | Yes | Name of the entity (e.g., User, Product) |
| `--no-model` | No | Skip generating the model/entity |
| `--no-service` | No | Skip generating the service layer |
| `--resources` | No | Generate CRUD endpoints and service methods ready for development |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:crud Product
spring-hex make:crud Product --resources
spring-hex make:crud Category --no-model
```

**Generated Files:**
- Entity class with JPA annotations
- Repository interface extending `JpaRepository`
- Service class with CRUD operations
- REST controller with CRUD endpoints
- Mapper class for DTO conversion

---

## Test Commands

### make:test
{: .d-inline-block }
Testing
{: .label .label-purple }

Generate a test class (unit or feature test).

**Usage:**
```bash
spring-hex make:test <name> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<name>` | Yes | Name of the class being tested (e.g., OrderService) |
| `--unit` | No | Generate unit test (default is feature/integration test) |
| `-p, --package` | No | Base package (auto-detected if not specified) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:test OrderService --unit
spring-hex make:test OrderController
```

**Generated Files:**
- JUnit test class with appropriate annotations and setup

---

### run:test
{: .d-inline-block }
Testing
{: .label .label-purple }

Execute tests using Maven or Gradle.

**Usage:**
```bash
spring-hex run:test [options]
```

| Option | Description |
|--------|-------------|
| `--unit` | Run only unit tests |
| `--feature` | Run only feature/integration tests |

**Example:**
```bash
spring-hex run:test
spring-hex run:test --unit
spring-hex run:test --feature
```

---

## Migration Commands

### make:migration
{: .d-inline-block }
Migration
{: .label .label-red }

Generate a database migration file.

**Usage:**
```bash
spring-hex make:migration <migrationName> [options]
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `<migrationName>` | Yes | Descriptive name for the migration (e.g., create_orders_table) |
| `--flyway` | No | Generate Flyway migration (default) |
| `--liquibase` | No | Generate Liquibase changeset |
| `--format` | No | Format for Liquibase: `sql`, `xml`, or `yaml` (default: sql) |
| `-o, --output` | No | Output directory (defaults to current directory) |

**Example:**
```bash
spring-hex make:migration create_orders_table
spring-hex make:migration add_status_column --liquibase --format xml
```

**Generated Files:**
- Flyway: Versioned SQL file with timestamp prefix (e.g., `V20260210123045__create_orders_table.sql`)
- Liquibase: Changeset file in specified format with master changelog update

---

### migrate
{: .d-inline-block }
Migration
{: .label .label-red }

Run all pending database migrations.

**Usage:**
```bash
spring-hex migrate
```

**Behavior:**
- Executes all pending migrations using the configured migration tool (Flyway or Liquibase)
- Updates migration history tracking

---

### migrate:rollback
{: .d-inline-block }
Migration
{: .label .label-red }

Rollback the last migration or multiple migrations.

**Usage:**
```bash
spring-hex migrate:rollback [options]
```

| Option | Description |
|--------|-------------|
| `--step` | Number of migrations to rollback (default: 1) |

**Example:**
```bash
spring-hex migrate:rollback
spring-hex migrate:rollback --step 3
```

**Note:** Rollback support varies by migration tool. Flyway requires undo migrations.

---

### migrate:status
{: .d-inline-block }
Migration
{: .label .label-red }

Display the status of all migrations.

**Usage:**
```bash
spring-hex migrate:status
```

**Output:**
Shows which migrations have been applied, pending migrations, and migration history.

---

### migrate:validate
{: .d-inline-block }
Migration
{: .label .label-red }

Validate applied migrations against available migration files.

**Usage:**
```bash
spring-hex migrate:validate
```

**Behavior:**
- Checks for migration file modifications
- Validates checksums
- Reports any inconsistencies

---

### migrate:repair
{: .d-inline-block }
Migration
{: .label .label-red }

Repair the migration history table.

**Usage:**
```bash
spring-hex migrate:repair
```

**Use Cases:**
- Fix checksum mismatches
- Remove failed migration entries
- Repair corrupted migration history

---

### migrate:fresh
{: .d-inline-block }
Migration
{: .label .label-red }

Drop all tables and re-run all migrations.

**Usage:**
```bash
spring-hex migrate:fresh --force
```

| Option | Required | Description |
|--------|----------|-------------|
| `--force` | Yes | Required safety flag to confirm destructive operation |

**Warning:** This command destroys all data in the database. Use only in development environments.

**Example:**
```bash
spring-hex migrate:fresh --force
```
