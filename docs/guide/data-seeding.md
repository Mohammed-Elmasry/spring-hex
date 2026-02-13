---
title: Data Seeding
parent: Guide
nav_order: 4
---

# Data Seeding

Spring-Hex provides factories and seeders for populating your database with development and test data — similar to Laravel's seeder/factory system.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

The data seeding system has three parts:

| Component | Purpose |
|:----------|:--------|
| **Factory** | Creates fake entity instances using Datafaker |
| **Seeder** | Populates the database using factories and repositories |
| **SeedRunner** | Dispatches `db:seed` commands to the right seeders |

## Factories

Factories generate fake entity instances for testing and seeding.

### Generating a Factory

```bash
spring-hex make:factory User
spring-hex make:factory OrderItem -a order
```

This creates a factory class with static `create()` methods:

```java
public class UserFactory {

    private static final Faker faker = new Faker();

    public static User create() {
        return User.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
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

### Nested Factories

For aggregate roots that contain child entities, call one factory from another:

```java
public class OrderFactory {

    private static final Faker faker = new Faker();

    public static Order create() {
        return Order.builder()
                .customer(CustomerFactory.create())
                .items(OrderItemFactory.create(3))
                .total(faker.number().randomDouble(2, 10, 500))
                .build();
    }
}
```

### Datafaker Dependency

Add Datafaker to your project:

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.4.2</version>
    <scope>test</scope>
</dependency>
```

If you use factories in seeders (not just tests), use `<scope>runtime</scope>` or omit the scope.

---

## Seeders

Seeders populate the database by combining factories with repositories.

### Generating a Seeder

```bash
spring-hex make:seeder UserSeeder --entity User
spring-hex make:seeder BookSeeder --entity Book
```

The first `make:seeder` call also auto-generates:
- `Seeder` interface
- `SeedRunner` infrastructure component

### Implementing a Seeder

Fill in the `seed()` method with your data logic:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements Seeder {

    private final UserRepository repository;

    @Override
    public void seed() {
        log.info("UserSeeder: seeding...");

        repository.saveAll(UserFactory.create(50));

        log.info("UserSeeder: done");
    }
}
```

---

## Execution Order

The `SeedRunner` contains an ordered list of seeder classes. When running `db:seed --all`, seeders execute in the order they appear in the list.

### Controlling the Order

Open your `SeedRunner` and add seeders to the `SEEDERS` list. Place independent entities first, then entities that depend on them:

```java
private static final List<Class<? extends Seeder>> SEEDERS = List.of(
        AuthorSeeder.class,      // no dependencies — runs first
        PublisherSeeder.class,   // no dependencies — runs second
        BookSeeder.class         // depends on Author and Publisher — runs last
);
```

This prevents Hibernate `TransientPropertyValueException` errors that occur when persisting an entity that references an unsaved entity.

---

## Running Seeders

### Run All Seeders

```bash
spring-hex db:seed --all
```

Executes every seeder in the `SEEDERS` list, in order.

### Run a Single Seeder

```bash
spring-hex db:seed UserSeeder
```

Runs only the specified seeder by bean name.

### What Happens Under the Hood

`db:seed` detects your build tool (Maven or Gradle) and runs:

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.arguments=--seed=UserSeeder

# Gradle
./gradlew bootRun --args=--seed=UserSeeder
```

The `SeedRunner` (a `CommandLineRunner`) picks up the `--seed=` argument and invokes the matching seeder.

---

## Typical Workflow

```bash
# 1. Generate factories for your entities
spring-hex make:factory Author
spring-hex make:factory Book

# 2. Generate seeders
spring-hex make:seeder AuthorSeeder --entity Author
spring-hex make:seeder BookSeeder --entity Book

# 3. Implement factory create() methods with Datafaker
# 4. Implement seeder seed() methods
# 5. Add seeders to SeedRunner.SEEDERS in dependency order

# 6. Run
spring-hex db:seed --all
```
