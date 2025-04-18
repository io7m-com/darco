darco
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.darco/com.io7m.darco.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.darco%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.darco/com.io7m.darco?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/darco/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/darco.svg?style=flat-square)](https://codecov.io/gh/io7m-com/darco)
![Java Version](https://img.shields.io/badge/17-java?label=java&color=e65cc3)

![com.io7m.darco](./src/site/resources/darco.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/darco/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/darco/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/darco/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/darco/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/darco/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/darco/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/darco/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/darco/actions?query=workflow%3Amain.windows.temurin.lts)|


# darco

The `darco` package provides a minimalist, opinionated API for database access.

### Features

  * Versioned schema upgrades provided by [trasco](https://www.io7m.com/software/trasco).
  * Pluggable query support.
  * Instrumented with [OpenTelemetry](https://www.opentelemetry.io).
  * [PostgreSQL](https://www.postgresql.org) support.
  * [SQLite](https://www.sqlite.org) support.
  * Written in pure Java 17.
  * [OSGi](https://www.osgi.org/) ready
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready
  * ISC license
  * High-coverage automated test suite

### Motivation

Many [io7m](https://www.io7m.com) packages talk to various databases. For
example:

  * [idstore](https://www.io7m.com/software/idstore) uses PostgreSQL as the
    underlying persistent data store.
  * [cardant](https://www.io7m.com/software/cardant) uses PostgreSQL as the
    underlying persistent data store.
  * [northpike](https://www.io7m.com/software/northpike) uses PostgreSQL as the
    underlying persistent data store.
  * [certusine](https://www.io7m.com/software/certusine) uses SQLite as the
    underlying persistent data store.
  * [looseleaf](https://www.io7m.com/software/looseleaf) uses SQLite as the
    underlying persistent data store.
  * ...

All of the listed projects abstracted the database behind a simplified set of
interfaces to allow for easier unit testing, and to allow for migrating to
different databases without having to completely rewrite all of the application
code. This meant that each one of those projects had to implement its own set of
nearly-identical interfaces and initialization code boilerplate.

In much the same manner as the
[hibiscus](https://www.io7m.com/software/hibiscus) API attempts to
provide a common interface around RPC clients, and the
[anethum](https://www.io7m.com/software/anethum) API attempts to
provide a common interface around parsers and serializers,
the `darco` package attempts to provide a common set of interfaces and abstract
classes to minimize the amount of essentially duplicated code between projects
that talk to databases.

You probably don't want to use this package for your own applications. It is
intended to be used to keep `io7m` packages consistent internally and to reduce
boilerplate.

### Building

```
$ mvn clean verify
```

### Usage

The `com.io7m.darco.api` package provides the basic interfaces that abstract
over a relational database. Projects are expected to extend various provided
abstract classes, and implement various interfaces to provide their own
database abstractions. The package exposes a database as small set of core
types listed in the following sections.

```
DDatabaseFactoryType databases;
DDatabaseConfigurationType configuration;

try (DDatabaseType database = databases.open(configuration, () -> {})) {
  try (DDatabaseConnectionType connection = database.openConnection()) {
    try (DDatabaseTransactionType transaction = connection.openTransaction()) {
      DDatabaseQueryType<String, DDatabaseUnit> query =
        transaction.query(SomeCustomQueryType.class);

      query.execute("Hello!");
      query.execute("Goodbye!");
      transaction.commit();
    }
  }
}
```

#### DDatabaseFactoryType

The `DDatabaseFactoryType` interface represents objects that provide
database instances. When a `DDatabaseFactoryType` is provided with a
[DDatabaseConfigurationType](#DDatabaseConfigurationType) value, it
yields a [DDatabaseType](#DDatabaseType) instance.

#### DDatabaseConfigurationType

The `DDatabaseConfigurationType` interface represents the basic configuration
properties required to open and/or connect to a database.

#### DDatabaseType

The `DDatabaseType` interface represents an open database. To interact with
the database, callers must call the `openConnection()` method to obtain a
`DDatabaseConnectionType` instance. In a typical request/response server
application, the application would obtain a new connection for each incoming
client request, and `close()` the connection after servicing the request.

#### DDatabaseConnectionType

The `DDatabaseConnectionType` interface represents an open connection to
a database. In order to perform database queries using the connection,
applications must create [transactions](#DDatabaseTransactionType) within
which to execute queries by calling the `openTransaction()` method.

#### DDatabaseTransactionType

The `DDatabaseTransactionType` interface represents a database transaction.
Applications perform work in transactions by requesting instances of
[queries](#DDatabaseQueryType) from the transaction. Transactions must be
explicitly _committed_, otherwise the work performed by queries inside the
transaction is implicitly _rolled back_.

#### DDatabaseQueryType

The `DDatabaseQueryType` interface represents a single query that can be
executed within a transaction. A query typically abstracts over one or more
SQL statements. Queries are strongly typed and are represented as functions
taking parameters of type `P` and returning results of type `R`.

Query implementations are provided by instances of the
`DDatabaseQueryProviderType` interface. Typically, a set of
`DDatabaseQueryProviderType` instances are registered using `ServiceLoader`.

### SQLite

Applications wishing to provide an abstraction over the
[SQLite](https://www.sqlite.org) database should extend the abstract classes
exposed by the `com.io7m.darco.sqlite` module.

### PostgreSQL

Applications wishing to provide an abstraction over the
[PostgreSQL](https://www.postgresql.org) database should extend the abstract
classes exposed by the `com.io7m.darco.postgres` module.

### Examples

The `com.io7m.darco.examples` module provides example implementations of
databases.

