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


package com.io7m.darco.examples.postgresql;

import com.io7m.darco.api.DDatabaseUnit;
import com.io7m.darco.examples.sqlite.ESWordPutType;

import java.sql.SQLException;

/**
 * The word-put query.
 */

public final class EPQWordPut
  extends EPQDatabaseQueryAbstract<String, DDatabaseUnit>
  implements ESWordPutType
{
  EPQWordPut(final EPQDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static EPQDatabaseQueryProviderType provider()
  {
    return EPQDatabaseQueryProvider.provide(ESWordPutType.class, EPQWordPut::new);
  }

  @Override
  protected DDatabaseUnit onExecute(
    final EPQDatabaseTransactionType transaction,
    final String text)
    throws SQLException
  {
    final var c = transaction.connection();

    try (var s = c.prepareStatement("INSERT INTO words VALUES (?)")) {
      s.setString(1, text);
      s.executeUpdate();
    }

    return DDatabaseUnit.UNIT;
  }
}
