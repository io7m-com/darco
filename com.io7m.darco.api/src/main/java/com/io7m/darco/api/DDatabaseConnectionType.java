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

import java.sql.Connection;

import static com.io7m.darco.api.DDatabaseTransactionCloseBehavior.ON_CLOSE_DO_NOTHING;

/**
 * A database connection.
 *
 * @param <T> The type of database transactions
 */

public interface DDatabaseConnectionType<T extends DDatabaseTransactionType>
  extends AutoCloseable
{
  /**
   * Register a closeable resource that will be closed when this connection
   * is closed.
   *
   * @param resource The resource
   * @param <R>      The precise type of resource
   *
   * @return The resource
   */

  <R extends AutoCloseable>
  R registerResource(R resource);

  @Override
  void close()
    throws DDatabaseException;

  /**
   * @return The underlying database connection
   */

  Connection connection();

  /**
   * Begin a new transaction. The transaction will be registered as a
   * closeable resource with this connection.
   *
   * @return The transaction
   *
   * @throws DDatabaseException On errors
   */

  default T openTransaction()
    throws DDatabaseException
  {
    return this.openTransaction(ON_CLOSE_DO_NOTHING);
  }

  /**
   * Begin a new transaction. The transaction will be registered as a
   * closeable resource with this connection.
   *
   * @param closeBehavior The close behavior
   *
   * @return The transaction
   *
   * @throws DDatabaseException On errors
   */

  T openTransaction(
    DDatabaseTransactionCloseBehavior closeBehavior)
    throws DDatabaseException;
}
