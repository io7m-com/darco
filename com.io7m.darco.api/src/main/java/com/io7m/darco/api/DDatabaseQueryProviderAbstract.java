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

import java.util.Objects;
import java.util.function.Function;

/**
 * A convenient abstract query provider.
 *
 * @param <T> The precise type of database transactions
 * @param <P> The type of query parameters
 * @param <R> The type of query results
 * @param <Q> The precise type of query
 */

public abstract class DDatabaseQueryProviderAbstract<
  T extends DDatabaseTransactionType,
  P,
  R,
  Q extends DDatabaseQueryType<P, R>>
  implements DDatabaseQueryProviderType<T, P, R, Q>
{
  private final Class<? extends Q> queryClass;
  private final Function<T, DDatabaseQueryType<P, R>> constructor;

  protected DDatabaseQueryProviderAbstract(
    final Class<? extends Q> inQueryClass,
    final Function<T, DDatabaseQueryType<P, R>> inConstructor)
  {
    this.queryClass =
      Objects.requireNonNull(inQueryClass, "queryClass");
    this.constructor =
      Objects.requireNonNull(inConstructor, "constructor");
  }

  @Override
  public final Class<? extends Q> queryClass()
  {
    return this.queryClass;
  }

  @Override
  public final DDatabaseQueryType<P, R> create(
    final T transaction)
  {
    return this.constructor.apply(transaction);
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
