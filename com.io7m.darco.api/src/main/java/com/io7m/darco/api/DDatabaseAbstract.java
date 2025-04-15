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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
  Q extends DDatabaseQueryProviderType<T, ?, ?, ?>>
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
      collectQueryProviders(inQueryProviders);
  }

  private static <
    T extends DDatabaseTransactionType,
    Q extends DDatabaseQueryProviderType<T, ?, ?, ?>>
  Map<Class<?>, Q> collectQueryProviders(
    final Collection<Q> inQueryProviders)
  {
    final var qmap = new HashMap<Class<?>, Q>(inQueryProviders.size());
    for (final var q : inQueryProviders) {
      final Class<?> qc = q.queryClass();
      final var existing = qmap.get(qc);
      if (existing != null) {
        final var sb = new StringBuilder(128);
        sb.append("Multiple query providers registered with the same class.");
        sb.append("\n");
        sb.append("Query class: ");
        sb.append(qc.getCanonicalName());
        sb.append("\n");
        sb.append("Existing registration: ");
        sb.append(existing.getClass().getCanonicalName());
        sb.append("\n");
        sb.append("Current registration: ");
        sb.append(q.getClass().getCanonicalName());
        sb.append("\n");
        throw new IllegalStateException(sb.toString());
      }
      qmap.put(qc, q);
    }
    return Map.copyOf(qmap);
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

    final var userPass =
      this.configuration.roles()
        .get(role);

    final var tracer =
      this.configuration.telemetry()
        .tracer();

    final var span =
      tracer.spanBuilder("DatabaseConnection")
        .setAttribute("db.system", DDatabaseKinds.sqlite().value())
        .startSpan();

    try {
      span.addEvent("RequestConnection");
      final var conn =
        this.dataSource.getConnection(
          userPass.userName(),
          userPass.password()
        );
      span.addEvent("ObtainedConnection");

      conn.setAutoCommit(false);
      return this.createConnection(span, conn, this.queryProviders);
    } catch (final SQLException e) {
      span.recordException(e);
      span.end();
      throw DDatabaseException.ofException(e);
    }
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
    return this.openConnection()
      .openTransaction(ON_CLOSE_CLOSE_CONNECTION);
  }

  @Override
  public final T openTransactionWithRole(
    final String role)
    throws DDatabaseException
  {
    Objects.requireNonNull(role, "role");

    return this.openConnectionWithRole(role)
      .openTransaction(ON_CLOSE_CLOSE_CONNECTION);
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
    return this.openConnectionWithRole(
      this.configuration.defaultRole().userName()
    );
  }

  @Override
  public final String toString()
  {
    return "[%s 0x%s]".formatted(
      this.getClass().getSimpleName(),
      Integer.toUnsignedString(this.hashCode(), 16)
    );
  }
}
