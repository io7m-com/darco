/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.darco.postgres;

import com.io7m.darco.api.DDatabaseConnectionType;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseFactoryType;
import com.io7m.darco.api.DDatabaseKinds;
import com.io7m.darco.api.DDatabaseQueryProviderType;
import com.io7m.darco.api.DDatabaseTransactionType;
import com.io7m.darco.api.DDatabaseType;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.trasco.api.TrArguments;
import com.io7m.trasco.api.TrEventExecutingSQL;
import com.io7m.trasco.api.TrEventType;
import com.io7m.trasco.api.TrEventUpgrading;
import com.io7m.trasco.api.TrExecutorConfiguration;
import com.io7m.trasco.api.TrSchemaRevisionSet;
import com.io7m.trasco.vanilla.TrExecutors;
import com.io7m.trasco.vanilla.TrSchemaRevisionSetParsers;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.io7m.trasco.api.TrExecutorUpgrade.FAIL_INSTEAD_OF_UPGRADING;
import static com.io7m.trasco.api.TrExecutorUpgrade.PERFORM_UPGRADES;
import static java.math.BigInteger.valueOf;

/**
 * An PostgreSQL-based database factory implementation.
 *
 * @param <C> The type of configuration
 * @param <D> The type of database
 * @param <N> The type of connections
 * @param <T> The type of transactions
 * @param <Q> The precise type of database query providers
 */

public abstract class DPQDatabaseFactory<
  C extends DPQDatabaseConfigurationType,
  N extends DDatabaseConnectionType<T>,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T, ?, ?, ?>,
  D extends DDatabaseType<C, N, T, Q>>
  implements DDatabaseFactoryType<C, N, T, Q, D>
{
  protected DPQDatabaseFactory()
  {

  }

  /**
   * @return The application ID
   */

  protected abstract RDottedName applicationId();

  /**
   * @return The logger
   */

  protected abstract Logger logger();

  /**
   * Create the actual database instance after everything has been configured
   * and the schema installed.
   *
   * @param configuration  The database configuration
   * @param source         The datasource
   * @param queryProviders The query providers
   * @param resources      The resources that should be closed with the database
   *
   * @return A new instance
   */

  protected abstract D onCreateDatabase(
    C configuration,
    DataSource source,
    List<Q> queryProviders,
    CloseableCollectionType<DDatabaseException> resources);

  /**
   * @param configuration The configuration of the database being created
   *
   * @return The database schema arguments
   */

  protected TrArguments onRequireDatabaseSchemaArguments(
    final C configuration)
  {
    return TrArguments.empty();
  }

  /**
   * @return The database schema text
   */

  protected abstract InputStream onRequireDatabaseSchemaXML();

  /**
   * Receive an event produced during database creation/upgrades.
   *
   * @param message The message
   */

  protected abstract void onEvent(String message);

  /**
   * Transform the data source used to connect to the database during
   * setup and/or upgrades.
   *
   * @param dataSource The dataSource
   *
   * @return The transformed data source
   */

  protected abstract DataSource onTransformDataSourceForSetup(
    DataSource dataSource);

  /**
   * Transform the data source used to connect to the database during normal
   * use (after setup and/or upgrades). This can be used to, for example, wrap
   * the data source in a pooling data source such as Hikari.
   *
   * @param dataSource The dataSource
   *
   * @see "https://github.com/brettwooldridge/HikariCP"
   *
   * @return The transformed data source
   */

  protected abstract DataSource onTransformDataSourceForUse(
    DataSource dataSource);

  /**
   * A method called after database schema upgrades have been completed
   * but before any work has been committed. If this exception throws any
   * exception, the overall upgrade will not be committed.
   *
   * @param configuration The configuration
   * @param connection    The database connection
   *
   * @throws Exception On errors
   */

  protected abstract void onPostUpgrade(
    C configuration,
    Connection connection)
    throws Exception;

  /**
   * @return The list of query providers
   */

  protected abstract List<Q> onRequireDatabaseQueryProviders();

  private void schemaVersionSet(
    final BigInteger version,
    final Connection connection)
    throws SQLException
  {
    final var applicationId =
      this.applicationId();

    final String statementText;
    if (Objects.equals(version, BigInteger.ZERO)) {
      statementText = "insert into schema_version (version_application_id, version_number) values (?, ?)";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setString(1, applicationId.value());
        statement.setLong(2, version.longValueExact());
        statement.execute();
      }
    } else {
      statementText = "update schema_version set version_number = ?";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setLong(1, version.longValueExact());
        statement.execute();
      }
    }
  }

  private Optional<BigInteger> schemaVersionGet(
    final Connection connection)
    throws SQLException
  {
    Objects.requireNonNull(connection, "connection");

    final var applicationId =
      this.applicationId();
    final var logger =
      this.logger();

    try {
      final var statementText =
        "SELECT version_application_id, version_number FROM schema_version";

      logger.debug("Execute: {}", statementText);

      try (var statement = connection.prepareStatement(statementText)) {
        try (var result = statement.executeQuery()) {
          if (!result.next()) {
            throw new SQLException("schema_version table is empty!");
          }
          final var applicationCA =
            result.getString(1);
          final var version =
            result.getLong(2);

          if (!Objects.equals(applicationCA, applicationId.value())) {
            throw new SQLException(
              String.format(
                "Database application ID is %s but should be %s",
                applicationCA,
                applicationId.value()
              )
            );
          }

          return Optional.of(valueOf(version));
        }
      }
    } catch (final SQLException e) {
      final var state = e.getSQLState();
      if (state == null) {
        throw e;
      }
      if (state.equals(PSQLState.UNDEFINED_TABLE.getState())) {
        connection.rollback();
        return Optional.empty();
      }

      throw e;
    }
  }

  @Override
  public final RDottedName kind()
  {
    return DDatabaseKinds.postgreSQL();
  }

  @Override
  public final D open(
    final C configuration,
    final Consumer<String> startupMessages)
    throws DDatabaseException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(startupMessages, "startupMessages");

    this.createOrUpgrade(configuration, startupMessages);
    return this.connect(configuration);
  }

  private D connect(
    final C configuration)
  {
    final var resources =
      createCloseableResources();

    final var url = new StringBuilder(128);
    url.append("jdbc:postgresql://");
    url.append(configuration.databaseAddress());
    url.append(':');
    url.append(configuration.databasePort());
    url.append('/');

    final var workerRole =
      configuration.workerRole();

    final var dataSource = new PGSimpleDataSource();
    dataSource.setURL(url.toString());
    dataSource.setUser(workerRole.userName());
    dataSource.setPassword(workerRole.password());
    dataSource.setDatabaseName(configuration.databaseName());
    dataSource.setSsl(configuration.databaseUseTLS());

    final var installDataSource =
      this.onTransformDataSourceForUse(dataSource);

    if (installDataSource instanceof final AutoCloseable closeable) {
      resources.add(closeable);
    }

    return this.onCreateDatabase(
      configuration,
      dataSource,
      this.onRequireDatabaseQueryProviders(),
      resources
    );
  }

  private void createOrUpgrade(
    final C configuration,
    final Consumer<String> startupMessages)
    throws DDatabaseException
  {
    final var resources =
      createCloseableResources();

    final var span =
      configuration.telemetry()
        .tracer()
        .spanBuilder("DatabaseSetup")
        .startSpan();

    final var schemaArguments =
      this.onRequireDatabaseSchemaArguments(configuration);

    try (var ignored0 = span.makeCurrent()) {
      try (var ignored1 = resources) {
        final var url = new StringBuilder(128);
        url.append("jdbc:postgresql://");
        url.append(configuration.databaseAddress());
        url.append(':');
        url.append(configuration.databasePort());
        url.append('/');

        final var ownerRole =
          configuration.ownerRole();

        final var dataSource = new PGSimpleDataSource();
        dataSource.setURL(url.toString());
        dataSource.setUser(ownerRole.userName());
        dataSource.setPassword(ownerRole.password());
        dataSource.setDatabaseName(configuration.databaseName());
        dataSource.setSsl(configuration.databaseUseTLS());

        final var installDataSource =
          this.onTransformDataSourceForSetup(dataSource);

        if (installDataSource instanceof final AutoCloseable closeable) {
          resources.add(closeable);
        }

        final var parsers = new TrSchemaRevisionSetParsers();
        final TrSchemaRevisionSet revisions;
        try (var stream = this.onRequireDatabaseSchemaXML()) {
          revisions = parsers.parse(URI.create("urn:source"), stream);
        }

        try (var connection = installDataSource.getConnection()) {
          connection.setAutoCommit(false);

          new TrExecutors().create(
            new TrExecutorConfiguration(
              this::schemaVersionGet,
              this::schemaVersionSet,
              event -> this.publishTrEvent(startupMessages, event),
              revisions,
              switch (configuration.upgrade()) {
                case UPGRADE_DATABASE -> PERFORM_UPGRADES;
                case DO_NOT_UPGRADE_DATABASE -> FAIL_INSTEAD_OF_UPGRADING;
              },
              schemaArguments,
              connection
            )
          ).execute();

          this.onPostUpgrade(configuration, connection);
          connection.commit();
        }
      }
    } catch (final Exception e) {
      throw DDatabaseException.ofException(e);
    } finally {
      span.end();
    }
  }

  private static CloseableCollectionType<DDatabaseException> createCloseableResources()
  {
    return CloseableCollection.create(() -> {
      return new DDatabaseException(
        "Closing a resource failed.",
        "error-resource-closing",
        Map.of(),
        Optional.empty()
      );
    });
  }

  private void publishEvent(
    final Consumer<String> startupMessages,
    final String message)
  {
    final var logger = this.logger();

    try {
      logger.trace("{}", message);
      startupMessages.accept(message);
    } catch (final Exception e) {
      logger.error("Ignored consumer exception: ", e);
    }

    try {
      this.onEvent(message);
    } catch (final Exception e) {
      logger.error("Ignored consumer exception: ", e);
    }
  }

  private void publishTrEvent(
    final Consumer<String> startupMessages,
    final TrEventType event)
  {
    if (event instanceof final TrEventExecutingSQL sql) {
      this.publishEvent(
        startupMessages,
        String.format("Executing SQL: %s", sql.statement())
      );
      return;
    }

    if (event instanceof final TrEventUpgrading upgrading) {
      this.publishEvent(
        startupMessages,
        String.format(
          "Upgrading database from version %s -> %s",
          upgrading.fromVersion(),
          upgrading.toVersion())
      );
      return;
    }
  }
}
