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


package com.io7m.darco.sqlite;

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
import org.slf4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteOpenMode;

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

import static com.io7m.darco.api.DDatabaseCreate.CREATE_DATABASE;
import static com.io7m.trasco.api.TrExecutorUpgrade.FAIL_INSTEAD_OF_UPGRADING;
import static com.io7m.trasco.api.TrExecutorUpgrade.PERFORM_UPGRADES;
import static java.math.BigInteger.valueOf;

/**
 * An SQLite-based database factory implementation.
 *
 * @param <C> The type of configuration
 * @param <D> The type of database
 * @param <N> The type of connections
 * @param <T> The type of transactions
 * @param <Q> The precise type of database query providers
 */

public abstract class DSDatabaseFactory<
  C extends DSDatabaseConfigurationType,
  N extends DDatabaseConnectionType<T>,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T>,
  D extends DDatabaseType<C, N, T, Q>>
  implements DDatabaseFactoryType<C, N, T, Q, D>
{
  protected DSDatabaseFactory()
  {

  }

  @Override
  public final RDottedName kind()
  {
    return DDatabaseKinds.sqlite();
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
   * @param resources      The resources to be closed when the database is closed
   *
   * @return A new instance
   */

  protected abstract D onCreateDatabase(
    C configuration,
    SQLiteDataSource source,
    List<Q> queryProviders,
    CloseableCollectionType<DDatabaseException> resources
  );

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
   * Adjust the SQLite configuration.
   *
   * @param config The configuration
   */

  protected abstract void onAdjustSQLiteConfig(
    SQLiteConfig config);

  /**
   * @return The list of query providers
   */

  protected abstract List<Q> onRequireDatabaseQueryProviders();

  private void schemaVersionSet(
    final BigInteger version,
    final Connection connection)
    throws SQLException
  {
    final String statementText;
    if (Objects.equals(version, BigInteger.ZERO)) {
      statementText = "insert into schema_version (version_application_id, version_number) values (?, ?)";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setString(1, this.applicationId().value());
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

    final var logger = this.logger();

    try {
      final var statementText =
        "SELECT version_application_id, version_number FROM schema_version";

      logger.debug("execute: {}", statementText);

      try (var statement = connection.prepareStatement(statementText)) {
        try (var result = statement.executeQuery()) {
          if (!result.next()) {
            throw new SQLException("schema_version table is empty!");
          }
          final var applicationCA =
            result.getString(1);
          final var version =
            result.getLong(2);

          final var applicationId =
            this.applicationId().value();

          if (!Objects.equals(applicationCA, applicationId)) {
            throw new SQLException(
              String.format(
                "Database application ID is %s but should be %s",
                applicationCA,
                applicationId
              )
            );
          }

          return Optional.of(valueOf(version));
        }
      }
    } catch (final SQLException e) {
      if (e.getErrorCode() == SQLiteErrorCode.SQLITE_ERROR.code) {
        connection.rollback();
        return Optional.empty();
      }
      throw e;
    }
  }

  private static void setWALMode(
    final Connection connection)
    throws SQLException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA journal_mode=WAL;");
    }
  }

  @Override
  public final D open(
    final C configuration,
    final Consumer<String> startupMessages)
    throws DDatabaseException
  {
    final var span =
      configuration.telemetry()
        .tracer()
        .spanBuilder("DatabaseSetup")
        .startSpan();

    try (var ignored0 = span.makeCurrent()) {
      final var dataSource =
        this.openDataSourceInSpan(configuration, startupMessages);

      return this.onCreateDatabase(
        configuration,
        dataSource,
        this.onRequireDatabaseQueryProviders(),
        createCloseableResources()
      );
    } finally {
      span.end();
    }
  }

  private SQLiteDataSource openDataSourceInSpan(
    final C configuration,
    final Consumer<String> startupMessages)
    throws DDatabaseException
  {
    final SQLiteDataSource dataSource;
    try {
      final var url = new StringBuilder(128);
      url.append("jdbc:sqlite:");
      url.append(configuration.file().toAbsolutePath());

      final var config = new SQLiteConfig();
      config.enforceForeignKeys(true);

      if (configuration.create() == CREATE_DATABASE) {
        config.setOpenMode(SQLiteOpenMode.CREATE);
      } else {
        config.resetOpenMode(SQLiteOpenMode.CREATE);
      }

      this.onAdjustSQLiteConfig(config);

      dataSource = new SQLiteDataSource(config);
      dataSource.setUrl(url.toString());

      final var parsers = new TrSchemaRevisionSetParsers();
      final TrSchemaRevisionSet revisions;
      try (var stream = this.onRequireDatabaseSchemaXML()) {
        revisions = parsers.parse(URI.create("urn:source"), stream);
      }

      final var arguments =
        this.onRequireDatabaseSchemaArguments(configuration);

      try (var connection = dataSource.getConnection()) {
        setWALMode(connection);
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
            arguments,
            connection
          )
        ).execute();

        connection.commit();
      }
    } catch (final Exception e) {
      throw DDatabaseException.ofException(e);
    }

    return dataSource;
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
    switch (event) {
      case final TrEventExecutingSQL sql -> {
        this.publishEvent(
          startupMessages,
          String.format("Executing SQL: %s", sql.statement())
        );
        return;
      }
      case final TrEventUpgrading upgrading -> {
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
}
