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


package com.io7m.darco.api;

import com.io7m.jmulticlose.core.CloseableCollectionType;
import io.opentelemetry.api.trace.Span;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.darco.api.DDatabaseTransactionCloseBehavior.ON_CLOSE_CLOSE_CONNECTION;

/**
 * An abstract implementation of the database type for databases.
 *
 * @param <C> The type of database configuration
 * @param <T> The type of database transactions
 * @param <N> The type of database connections
 * @param <Q> The precise type of database query providers
 */

public abstract class DDatabaseAbstract<
  C extends DDatabaseConfigurationType,
  N extends DDatabaseConnectionType<T>,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T>>
  implements DDatabaseType<C, N, T, Q>
{
  private final C configuration;
  private final DataSource dataSource;
  private final Map<Class<?>, Q> queryProviders;
  private final CloseableCollectionType<DDatabaseException> resources;

  protected DDatabaseAbstract(
    final C inConfiguration,
    final DataSource inDataSource,
    final Collection<Q> inQueryProviders,
    final CloseableCollectionType<DDatabaseException> inResources)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.dataSource =
      Objects.requireNonNull(inDataSource, "dataSource");
    this.resources =
      Objects.requireNonNull(inResources, "inResources");
    this.queryProviders =
      inQueryProviders.stream()
        .map(p -> Map.entry(p.queryClass(), p))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public final void close()
    throws DDatabaseException
  {
    this.resources.close();
  }

  @Override
  public final N openConnectionWithRole(
    final String role)
    throws DDatabaseException
  {
    Objects.requireNonNull(role, "role");
    return this.openConnection();
  }

  @Override
  public final C configuration()
  {
    return this.configuration;
  }

  @Override
  public final T openTransaction()
    throws DDatabaseException
  {
    final var connection = this.openConnection();
    return connection.openTransaction(ON_CLOSE_CLOSE_CONNECTION);
  }

  @Override
  public final T openTransactionWithRole(
    final String role)
    throws DDatabaseException
  {
    Objects.requireNonNull(role, "role");

    final var connection = this.openConnectionWithRole(role);
    return connection.openTransaction(ON_CLOSE_CLOSE_CONNECTION);
  }

  /**
   * Create a new connection.
   *
   * @param span       The connection span
   * @param connection The underlying SQL connection
   * @param queries    The query map
   *
   * @return The connection
   */

  protected abstract N createConnection(
    Span span,
    Connection connection,
    Map<Class<?>, Q> queries)
    throws DDatabaseException;

  @Override
  public final N openConnection()
    throws DDatabaseException
  {
    final var tracer =
      this.configuration.telemetry()
        .tracer();

    final var span =
      tracer.spanBuilder("DatabaseConnection")
        .setAttribute("db.system", DDatabaseKinds.sqlite().value())
        .startSpan();

    try {
      span.addEvent("RequestConnection");
      final var conn = this.dataSource.getConnection();
      span.addEvent("ObtainedConnection");

      conn.setAutoCommit(false);
      return this.createConnection(span, conn, this.queryProviders);
    } catch (final SQLException e) {
      span.recordException(e);
      span.end();
      throw DDatabaseException.ofException(e);
    }
  }
}
