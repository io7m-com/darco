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

import java.sql.SQLException;
import java.util.Objects;

/**
 * An abstract query implementation.
 *
 * @param <T> The precise type of database transactions
 * @param <P> The type of query parameters
 * @param <R> The type of query results
 */

public abstract class DDatabaseQueryAbstract<
  T extends DDatabaseTransactionType,
  P,
  R>
  implements DDatabaseQueryType<P, R>
{
  private final T currentTransaction;

  protected DDatabaseQueryAbstract(
    final T inTransaction)
  {
    this.currentTransaction =
      Objects.requireNonNull(inTransaction, "transaction");
  }

  @Override
  public final R execute(
    final P parameters)
    throws DDatabaseException
  {
    Objects.requireNonNull(parameters, "parameters");

    try {
      return this.onExecute(this.currentTransaction, parameters);
    } catch (final SQLException e) {
      throw DDatabaseException.ofException(e);
    }
  }

  protected abstract R onExecute(
    T transaction,
    P parameters)
    throws DDatabaseException, SQLException;

}
