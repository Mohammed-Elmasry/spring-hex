---
title: Path Defaults
parent: Reference
nav_order: 2
---

# Path Defaults

Spring-Hex CLI uses configurable path patterns to determine where generated code is placed. This reference documents all default path patterns and shows how to customize them.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

Path patterns use placeholder variables that are replaced at generation time. Variables are enclosed in curly braces (e.g., `{aggregate}`). The tool resolves these patterns to create fully-qualified package names.

**Common Variables:**
- `{aggregate}` - The aggregate name provided via the `-a` flag
- `{name}` - The entity or component name (used in CRUD paths)

All paths are relative to your base package, which is either:
1. Specified via the `-p/--package` flag
2. Auto-detected from your existing source files
3. Configured in `.hex/config.yml`

---

## Hexagonal Architecture Paths

Default path patterns for hexagonal architecture components.

| Key | Default Pattern | Variables | Resolved Example |
|-----|----------------|-----------|------------------|
| `model` | `domain.{aggregate}.model` | `aggregate` | `com.app.domain.order.model` |
| `command` | `domain.{aggregate}.command` | `aggregate` | `com.app.domain.order.command` |
| `query` | `domain.{aggregate}.query` | `aggregate` | `com.app.domain.order.query` |
| `event` | `domain.{aggregate}.event` | `aggregate` | `com.app.domain.order.event` |
| `port_in` | `domain.{aggregate}.port.in` | `aggregate` | `com.app.domain.order.port.in` |
| `port_out` | `domain.{aggregate}.port.out` | `aggregate` | `com.app.domain.order.port.out` |
| `value_object` | `domain.{aggregate}.model` | `aggregate` | `com.app.domain.order.model` |
| `aggregate_root` | `domain.{aggregate}` | `aggregate` | `com.app.domain.order` |
| `adapter` | `infrastructure.{category}.{aggregate}` | `aggregate`, `category` | `com.app.infrastructure.persistence.order` |
| `persistence` | `infrastructure.persistence.{aggregate}` | `aggregate` | `com.app.infrastructure.persistence.order` |
| `controller` | `infrastructure.web.{aggregate}` | `aggregate` | `com.app.infrastructure.web.order` |
| `request` | `infrastructure.web.{aggregate}` | `aggregate` | `com.app.infrastructure.web.order` |
| `response` | `infrastructure.web.{aggregate}` | `aggregate` | `com.app.infrastructure.web.order` |
| `mediator` | `application.mediator` | (none) | `com.app.application.mediator` |
| `config` | `infrastructure.config` | (none) | `com.app.infrastructure.config` |
| `factory` | `infrastructure.factory.{aggregate}` | `aggregate` | `com.app.infrastructure.factory.order` |
| `seeder` | `infrastructure.seeder` | (none) | `com.app.infrastructure.seeder` |

---

## CRUD Paths

Default path patterns for CRUD-style components (traditional MVC architecture).

| Key | Default Pattern | Variables | Resolved Example |
|-----|----------------|-----------|------------------|
| `model` | `{name}.model` | `name` | `com.app.user.model` |
| `repository` | `{name}.repository` | `name` | `com.app.user.repository` |
| `service` | `{name}.service` | `name` | `com.app.user.service` |
| `controller` | `{name}.controller` | `name` | `com.app.user.controller` |
| `mapper` | `{name}.mapper` | `name` | `com.app.user.mapper` |
| `dto` | `{name}.dto` | `name` | `com.app.user.dto` |

---

## Migration Paths

Database migration files are placed in resource directories, not package paths.

| Migration Tool | Default Path | Example |
|----------------|--------------|---------|
| Flyway | `src/main/resources/db/migration` | `V20260210120000__create_orders_table.sql` |
| Liquibase | `src/main/resources/db/changelog` | `changelog-20260210-create-orders.sql` |

---

## Test Paths

Test files mirror the structure of source files but are placed in the test source directory.

| Test Type | Pattern | Example |
|-----------|---------|---------|
| Unit Test | Same package as source class | `src/test/java/com/app/domain/order/OrderServiceTest.java` |
| Feature Test | Same package as source class | `src/test/java/com/app/infrastructure/order/OrderControllerTest.java` |

---

## Customizing Paths

You can override default paths in your `.hex/config.yml` file.

### Complete Configuration Example

```yaml
basePackage: com.mycompany.app

paths:
  hexagonal:
    model: domain.{aggregate}.core
    command: application.{aggregate}.commands
    query: application.{aggregate}.queries
    event: domain.{aggregate}.events
    port_in: application.{aggregate}.ports.input
    port_out: application.{aggregate}.ports.output
    value_object: domain.{aggregate}.values
    aggregate_root: domain.{aggregate}
    adapter: infrastructure.adapters.{aggregate}
    repository: infrastructure.persistence.{aggregate}
    controller: infrastructure.web.{aggregate}
    mapper: infrastructure.persistence.{aggregate}.mappers
    request: infrastructure.web.{aggregate}.requests
    response: infrastructure.web.{aggregate}.responses
    mediator: application.cqrs
    config: configuration

  crud:
    model: modules.{name}.domain
    repository: modules.{name}.data
    service: modules.{name}.business
    controller: modules.{name}.api
    mapper: modules.{name}.data.mappers
    dto: modules.{name}.api.dtos
```

### Partial Configuration

You only need to specify paths you want to override. Unspecified paths will use defaults.

```yaml
basePackage: com.mycompany.app

paths:
  hexagonal:
    command: application.commands.{aggregate}
    query: application.queries.{aggregate}
    # All other paths use defaults
```

### Using Category Subdirectories

The `make:adapter` command supports a `--category` option to organize adapters by type.

```bash
spring-hex make:adapter JpaOrderRepository -a order --port OrderRepository --category persistence
```

With default paths, this generates:
```
com.app.infrastructure.order.adapter.persistence.JpaOrderRepository
```

You can customize the adapter path to control this structure:

```yaml
paths:
  hexagonal:
    adapter: infrastructure.{aggregate}.adapters.{category}
```

---

## Path Resolution Process

When you run a command like:

```bash
spring-hex make:command CreateOrder -a order -p com.mycompany.app
```

The tool follows this resolution process:

1. **Base Package**: Uses `com.mycompany.app` from the `-p` flag (or auto-detects)
2. **Path Pattern**: Looks up the `command` path pattern from config (default: `domain.{aggregate}.command`)
3. **Variable Substitution**: Replaces `{aggregate}` with `order` → `domain.order.command`
4. **Full Package**: Combines base package and resolved path → `com.mycompany.app.domain.order.command`
5. **File System Path**: Converts to directory structure → `src/main/java/com/mycompany/app/domain/order/command/`

---

## Best Practices

### Hexagonal Architecture

For hexagonal architecture, maintain clear separation between layers:

- **Domain layer** (`domain.*`) - Pure business logic, no framework dependencies
- **Application layer** (`application.*`) - Use cases, ports, CQRS components
- **Infrastructure layer** (`infrastructure.*`) - Adapters, controllers, persistence

### CRUD Architecture

For CRUD projects, organize by feature module:

```
com.app
  ├── user
  │   ├── model
  │   ├── repository
  │   ├── service
  │   └── controller
  ├── product
  │   ├── model
  │   ├── repository
  │   ├── service
  │   └── controller
```

### Consistent Naming

Use consistent variable names in your path patterns:
- `{aggregate}` for hexagonal architecture
- `{name}` for CRUD modules

### Avoid Deep Nesting

While you can create deeply nested package structures, keep them reasonable:

**Good:**
```yaml
command: domain.{aggregate}.command
```

**Avoid:**
```yaml
command: com.internal.{aggregate}.domain.layer.cqrs.commands.module
```

---

## Validation

Spring-Hex CLI validates path configurations at runtime:

- **Missing variables**: Warns if a path pattern references undefined variables
- **Invalid characters**: Prevents invalid package names
- **Circular references**: Detects and prevents circular path definitions

Run `spring-hex init` to generate a valid configuration file with all available path options.
