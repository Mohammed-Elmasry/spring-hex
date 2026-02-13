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
| **Factory** | Creates and persists fake entity instances using Datafaker |
| **Seeder** | Orchestrates factories to populate the database |
| **SeedRunner** | Dispatches `db:seed` commands to the right seeders |

## Factories

Factories generate fake entity instances. Like Laravel, `create()` persists to the database and `make()` builds in memory only.

### Generating a Factory

```bash
spring-hex make:factory UserEntity
spring-hex make:factory OrderItemEntity -a order
```

This creates a factory `@Component` with a repository injection. If the repository doesn't exist yet, it's auto-generated.

```java
@Component
@RequiredArgsConstructor
public class UserEntityFactory {

    private final UserEntityRepository repository;
    private static final Faker faker = new Faker();

    public UserEntity make() {
        return UserEntity.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .build();
    }

    public List<UserEntity> make(int count) { ... }

    public UserEntity create() {
        return repository.save(make());
    }

    public List<UserEntity> create(int count) {
        return repository.saveAll(make(count));
    }
}
```

### create() vs make()

| Method | Behavior | Use Case |
|:-------|:---------|:---------|
| `create()` | Builds and **saves** to database | Seeders, integration tests |
| `create(n)` | Builds and saves `n` entities | Bulk seeding |
| `make()` | Builds in **memory only** | Unit tests, assertions |
| `make(n)` | Builds `n` entities in memory | Batch unit tests |

### Nested Factories

For entities with relationships (e.g., BookEntity belongs to AuthorEntity), call the dependency factory's `create()` to persist it first:

```java
@Component
@RequiredArgsConstructor
public class BookEntityFactory {

    private final BookEntityRepository repository;
    private final AuthorEntityFactory authorEntityFactory;
    private static final Faker faker = new Faker();

    public BookEntity make() {
        return BookEntity.builder()
                .title(faker.book().title())
                .author(authorEntityFactory.create())  // persisted before Book
                .build();
    }

    public BookEntity create() {
        return repository.save(make());
    }
}
```

Because `authorEntityFactory.create()` saves the AuthorEntity first, Hibernate won't throw a `TransientPropertyValueException`.

### Datafaker Dependency

Add Datafaker to your project:

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.4.2</version>
</dependency>
```

---

## Seeders

Seeders orchestrate factories to populate the database.

### Generating a Seeder

```bash
spring-hex make:seeder UserSeeder --entity UserEntity
spring-hex make:seeder BookSeeder --entity BookEntity
```

The first `make:seeder` call also auto-generates:
- `Seeder` interface
- `SeedRunner` infrastructure component

### Implementing a Seeder

Fill in the `seed()` method — the factory handles persistence:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements Seeder {

    private final UserEntityFactory factory;

    @Override
    public void seed() {
        log.info("UserSeeder: seeding...");

        factory.create(50);  // creates and saves 50 users

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
# 1. Generate factories (repos auto-created if missing)
spring-hex make:factory AuthorEntity
spring-hex make:factory BookEntity

# 2. Generate seeders
spring-hex make:seeder AuthorSeeder --entity AuthorEntity
spring-hex make:seeder BookSeeder --entity BookEntity

# 3. Implement factory make() methods with Datafaker
# 4. Implement seeder seed() methods (call factory.create())
# 5. Add seeders to SeedRunner.SEEDERS in dependency order

# 6. Run
spring-hex db:seed --all
```
