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

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract database connection.
 *
 * @param <C> The type of database configuration
 * @param <T> The type of database transactions
 * @param <Q> The precise type of database query providers
 */

public abstract class DDatabaseConnectionAbstract<
  C extends DDatabaseConfigurationType,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T>>
  implements DDatabaseConnectionType<T>
{
  private final Connection connection;
  private final Span connectionSpan;
  private final C configuration;
  private final Map<Class<?>, Q> queryMap;

  protected DDatabaseConnectionAbstract(
    final C inConfiguration,
    final Span inSpan,
    final Connection inConnection,
    final Map<Class<?>, Q> inQueryMap)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "inConfiguration");
    this.connection =
      Objects.requireNonNull(inConnection, "conn");
    this.connectionSpan =
      Objects.requireNonNull(inSpan, "span");
    this.queryMap =
      Objects.requireNonNull(inQueryMap, "queries");
  }

  @Override
  public final Connection connection()
  {
    return this.connection;
  }

  /**
   * Create a new transaction.
   *
   * @param closeBehavior The close behavior
   * @param transactionSpan The transaction span
   * @param queries         The query provider map
   *
   * @return A new transaction
   */

  protected abstract T createTransaction(
    DDatabaseTransactionCloseBehavior closeBehavior,
    Span transactionSpan,
    Map<Class<?>, Q> queries
  );

  @Override
  public final T openTransaction(
    final DDatabaseTransactionCloseBehavior closeBehavior)
  {
    Objects.requireNonNull(closeBehavior, "closeBehavior");

    final var transactionSpan =
      this.configuration.telemetry()
        .tracer()
        .spanBuilder("DatabaseTransaction")
        .setParent(Context.current().with(this.connectionSpan))
        .startSpan();

    return this.createTransaction(
      closeBehavior,
      transactionSpan,
      this.queryMap
    );
  }

  /**
   * @return The configuration used to open this database
   */

  protected final C configuration()
  {
    return this.configuration;
  }

  @Override
  public final void close()
    throws DDatabaseException
  {
    try {
      if (!this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (final SQLException e) {
      this.connectionSpan.recordException(e);
      throw DDatabaseException.ofException(e);
    } finally {
      this.connectionSpan.end();
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
