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

import com.io7m.lanark.core.RDottedName;

import java.util.function.Consumer;

/**
 * The type of database factories.
 *
 * @param <C> The precise type of database configuration
 * @param <N> The precise type of database connections
 * @param <T> The precise type of database transactions
 * @param <Q> The precise type of database query providers
 * @param <D> The precise type of database implementation
 */

public interface DDatabaseFactoryType<
  C extends DDatabaseConfigurationType,
  N extends DDatabaseConnectionType<T>,
  T extends DDatabaseTransactionType,
  Q extends DDatabaseQueryProviderType<T, ?, ?, ?>,
  D extends DDatabaseType<C, N, T, Q>>
{
  /**
   * @return The database kind (such as "org.sqlite", "org.postgresql")
   */

  RDottedName kind();

  /**
   * Open a database.
   *
   * @param configuration   The database configuration
   * @param startupMessages A function that will receive startup messages
   *
   * @return A database
   *
   * @throws DDatabaseException On errors
   */

  D open(
    C configuration,
    Consumer<String> startupMessages)
    throws DDatabaseException;
}
