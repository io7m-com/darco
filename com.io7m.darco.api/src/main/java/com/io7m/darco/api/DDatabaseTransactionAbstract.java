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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.opentelemetry.api.trace.SpanKind.INTERNAL;

/**
 * An abstract implementation of the transaction type.
 *
 * @param <C> The type of database configuration
 * @param <T> The type of database transactions
 * @param <N> The type of database connections
 * @param <Q> The precise type of database query providers
 */

public abstract class DDatabaseTransactionAbstract<
  C extends DDatabaseConfigurationType,
  N extends DDatabaseConnectionType<T>,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T, ?, ?, ?>>
  implements DDatabaseTransactionType
{
  private final DDatabaseTransactionCloseBehavior closeBehavior;
  private final C configuration;
  private final N connection;
  private final Span transactionSpan;
  private final Map<Class<?>, Q> queries;
  private final CloseableCollectionType<DDatabaseException> resources;
  private final HashMap<Class<?>, Object> values;

  protected DDatabaseTransactionAbstract(
    final DDatabaseTransactionCloseBehavior inCloseBehavior,
    final C inConfiguration,
    final N inConnection,
    final Span inTransactionScope,
    final Map<Class<?>, Q> inQueries)
  {
    this.closeBehavior =
      Objects.requireNonNull(inCloseBehavior, "closeBehavior");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "inConfiguration");
    this.connection =
      Objects.requireNonNull(inConnection, "connection");
    this.transactionSpan =
      Objects.requireNonNull(inTransactionScope, "inMetricsScope");
    this.queries =
      Objects.requireNonNull(inQueries, "queries");
    this.resources =
      CloseableCollection.create(() -> {
        return new DDatabaseException(
          "One or more resources could not be closed.",
          "error-resource-close",
          Map.of(),
          Optional.empty()
        );
      });

    this.values =
      new HashMap<>();
  }

  @Override
  public final <V> void put(
    final Class<? extends V> clazz,
    final V value)
  {
    if (value instanceof final AutoCloseable closeable) {
      this.resources.add(closeable);
    }
    this.values.put(clazz, value);
  }

  @Override
  public final <V> V get(
    final Class<V> clazz)
  {
    return Optional.ofNullable(this.values.get(clazz))
      .map(clazz::cast)
      .orElseThrow(() -> {
        return new IllegalStateException(
          "No object registered for class %s".formatted(clazz)
        );
      });
  }

  @Override
  public final Connection connection()
  {
    return this.connection.connection();
  }

  @Override
  public final Span createSubSpan(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    return this.configuration.telemetry()
      .tracer()
      .spanBuilder(name)
      .setParent(Context.current().with(this.transactionSpan))
      .setAttribute("db.system", DDatabaseKinds.sqlite().value())
      .setSpanKind(INTERNAL)
      .startSpan();
  }

  @Override
  public final void close()
    throws DDatabaseException
  {
    try {
      this.rollback();
    } catch (final Exception e) {
      this.transactionSpan.recordException(e);
      throw e;
    } finally {
      this.transactionSpan.end();

      switch (this.closeBehavior) {
        case ON_CLOSE_CLOSE_CONNECTION -> {
          this.connection.close();
        }
        case ON_CLOSE_DO_NOTHING -> {
          // Nothing to do!
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <P, R, S extends DDatabaseQueryType<P, R>> S query(
    final Class<S> queryClass)
    throws DDatabaseException
  {
    final var provider = this.queries.get(queryClass);
    if (provider != null) {
      return (S) (Object) provider.create((T) this);
    }

    throw new DDatabaseException(
      "Unsupported query type: %s".formatted(queryClass),
      "error-unsupported-query-class",
      Map.of(),
      Optional.empty()
    );
  }

  @Override
  public final void rollback()
    throws DDatabaseException
  {
    try {
      this.connection.connection().rollback();
    } catch (final SQLException e) {
      this.transactionSpan.recordException(e);
      throw DDatabaseException.ofException(e);
    }
  }

  @Override
  public final void commit()
    throws DDatabaseException
  {
    try {
      this.connection.connection().commit();
    } catch (final SQLException e) {
      this.transactionSpan.recordException(e);
      throw DDatabaseException.ofException(e);
    }
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
