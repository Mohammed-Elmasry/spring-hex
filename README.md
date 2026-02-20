# Spring Hex CLI

A command-line tool that generates [hexagonal architecture](https://alistair.cockburn.us/hexagonal-architecture/) scaffolding for Spring Boot projects. Stop writing boilerplate — scaffold domain models, ports, adapters, CQRS infrastructure, and full bounded contexts in seconds.

## Features

- **Hexagonal Architecture** — Domain, port, and adapter layers with proper dependency inversion
- **CQRS** — CommandBus/QueryBus with auto-wired handlers
- **DDD Building Blocks** — Aggregates, value objects, domain events, bounded contexts
- **Multiple Data Stores** — JPA, MongoDB, and Redis adapter generation
- **Database Migrations** — Flyway and Liquibase support with auto-detection
- **Simple CRUD** — Traditional MVC stack when hexagonal complexity isn't needed
- **Data Seeding** — Factories with Datafaker and seeders for dev/test data
- **Customizable** — Override any generated path via `.hex/config.yml`

## Requirements

- Java 17+
- Maven 3.6+ (for building from source)
- A Spring Boot 3.x project (target project)

## Installation

### Quick Install (Recommended)

```bash
curl -fsSL https://raw.githubusercontent.com/Spring-hex/Spring-hex.github.io/master/install.sh | bash
source ~/.zshrc  # or ~/.bashrc
```

### Build from Source

```bash
git clone https://github.com/Spring-hex/Spring-hex.github.io.git
cd Spring-hex.github.io
mvn clean package
java -jar target/spring-hex-cli-1.0.0.jar --version
```

See the [installation guide](https://spring-hex.github.io/getting-started/installation) for manual install options and troubleshooting.

## Quick Start

```bash
# Navigate to your Spring Boot project
cd my-spring-app

# 1. Initialize configuration
spring-hex init

# 2. Set up CQRS infrastructure (CommandBus, QueryBus)
spring-hex make:mediator

# 3. Generate a full bounded context
spring-hex make:module Order
```

This generates a complete `Order` module:

```
src/main/java/com/example/
  domain/
    order/
      model/          Order.java
      command/         CreateOrderCommand.java, CreateOrderCommandHandler.java
      query/           GetOrderQuery.java, GetOrderQueryHandler.java
      event/           OrderCreatedEvent.java, OrderCreatedEventListener.java
      port/in/         OrderInputPort.java
      port/out/        OrderOutputPort.java
  infrastructure/
    persistence/order/ OrderEntity.java, OrderJpaRepository.java, OrderRepositoryAdapter.java
    web/order/         OrderController.java, OrderMapper.java
```

## Commands

### Setup

| Command | Description |
|---------|-------------|
| `init` | Initialize `.hex/config.yml` in the current project |

### Hexagonal Architecture

| Command | Description |
|---------|-------------|
| `make:module <name>` | Generate a complete bounded context |
| `make:mediator` | Generate CQRS infrastructure (CommandBus, QueryBus) |
| `make:aggregate <name>` | Aggregate root + ID value object |
| `make:model <name> -a <aggregate>` | Domain model |
| `make:command <name> -a <aggregate>` | Command + handler |
| `make:query <name> -a <aggregate>` | Query + handler |
| `make:event <name> -a <aggregate>` | Domain event + listener |
| `make:port <name> -a <aggregate>` | Port interface |
| `make:adapter <name> -a <aggregate> --port <port>` | Adapter implementation |
| `make:repository <name> -a <aggregate>` | Repository port + adapter |
| `make:entity <name> -a <aggregate>` | JPA entity |
| `make:mapper <name> -a <aggregate>` | DTO mapper |
| `make:controller <aggregate>` | REST controller |
| `make:request <name> -a <aggregate>` | Request DTO |
| `make:response <name> -a <aggregate>` | Response DTO |
| `make:value-object <name> -a <aggregate>` | Immutable value object |

### Simple CRUD

| Command | Description |
|---------|-------------|
| `make:crud <name>` | Model, entity, repository, service, mapper, and controller |
| `make:crud <name> --resources` | Same as above with full CRUD endpoints pre-built |

### Data Seeding

| Command | Description |
|---------|-------------|
| `make:factory <name>` | Data factory class with Datafaker |
| `make:seeder <name> --entity <entity>` | Database seeder class |
| `db:seed <SeederName>` | Run a specific seeder |
| `db:seed --all` | Run all seeders |

### Database Migrations

| Command | Description |
|---------|-------------|
| `make:migration <name>` | Generate a migration file (Flyway or Liquibase) |
| `migrate` | Run pending migrations |
| `migrate:rollback` | Roll back migrations |
| `migrate:status` | Show migration status |
| `migrate:validate` | Validate applied migrations |
| `migrate:repair` | Repair migration history |
| `migrate:fresh` | Drop all tables and re-migrate (requires `--force`) |

### Testing

| Command | Description |
|---------|-------------|
| `make:test <name>` | Generate a test class |
| `run:test` | Run tests (`--unit`, `--feature`) |

## Configuration

Running `spring-hex init` creates `.hex/config.yml`:

```yaml
base-package: com.example.myapp

paths:
  model: domain.{aggregate}.model
  command: domain.{aggregate}.command
  query: domain.{aggregate}.query
  port-in: domain.{aggregate}.port.in
  port-out: domain.{aggregate}.port.out
  persistence: infrastructure.persistence.{aggregate}
  controller: infrastructure.web.{aggregate}

crud:
  model: "{name}.model"
  entity: "{name}.entity"
  repository: "{name}.repository"
  service: "{name}.service"
  controller: "{name}.web"
  mapper: "{name}.mapper"
```

The base package is auto-detected from your `pom.xml` or Spring Boot main class. All paths are customizable.

## Documentation

Full documentation is available at **[spring-hex.github.io](https://spring-hex.github.io/)** — including tutorials, architecture guides, and a complete command reference.

## License

[MIT](LICENSE)
