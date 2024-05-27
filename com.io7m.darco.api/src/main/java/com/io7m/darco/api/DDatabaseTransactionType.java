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

import java.sql.Connection;

/**
 * A database transaction. If the transaction is closed, it is automatically
 * rolled back.
 */

public interface DDatabaseTransactionType
  extends AutoCloseable
{
  /**
   * @return The underlying database connection
   */

  Connection connection();

  @Override
  void close()
    throws DDatabaseException;

  /**
   * Obtain a query for the transaction.
   *
   * @param queryClass The query type
   * @param <Q>        The query type
   * @param <P>        The query parameter type
   * @param <R>        The query return type
   *
   * @return The query
   *
   * @throws DDatabaseException On errors
   */

  <P, R, Q extends DDatabaseQueryType<P, R>> Q query(
    Class<Q> queryClass)
    throws DDatabaseException;

  /**
   * Roll back the transaction.
   *
   * @throws DDatabaseException On errors
   */

  void rollback()
    throws DDatabaseException;

  /**
   * Commit the transaction.
   *
   * @throws DDatabaseException On errors
   */

  void commit()
    throws DDatabaseException;

  /**
   * Create a new span as a subset of the current transaction span. This can
   * be used to measure the times for individual queries within a transaction.
   *
   * @param name The span name
   *
   * @return The span
   */

  Span createSubSpan(
    String name);
}
