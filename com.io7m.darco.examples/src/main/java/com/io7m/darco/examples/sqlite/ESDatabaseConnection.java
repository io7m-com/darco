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


package com.io7m.darco.examples.sqlite;

import com.io7m.darco.api.DDatabaseConnectionAbstract;
import com.io7m.darco.api.DDatabaseTransactionCloseBehavior;
import io.opentelemetry.api.trace.Span;

import java.sql.Connection;
import java.util.Map;

final class ESDatabaseConnection
  extends DDatabaseConnectionAbstract<
  ESDatabaseConfiguration,
  ESDatabaseTransactionType,
  ESDatabaseQueryProviderType<?, ?, ?>>
  implements ESDatabaseConnectionType
{
  ESDatabaseConnection(
    final ESDatabase database,
    final Span span,
    final Connection connection,
    final Map<Class<?>, ESDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    super(database.configuration(), span, connection, queries);
  }

  @Override
  protected ESDatabaseTransactionType createTransaction(
    final DDatabaseTransactionCloseBehavior closeBehavior,
    final Span transactionSpan,
    final Map<Class<?>, ESDatabaseQueryProviderType<?, ?, ?>> queries)
  {
    return new ESDatabaseTransaction(
      closeBehavior,
      this.configuration(),
      this,
      transactionSpan,
      queries
    );
  }
}
