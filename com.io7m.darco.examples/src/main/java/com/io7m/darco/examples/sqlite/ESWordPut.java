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

import com.io7m.darco.api.DDatabaseUnit;

import java.sql.SQLException;

/**
 * The word-put query.
 */

public final class ESWordPut
  extends ESDatabaseQueryAbstract<String, DDatabaseUnit>
  implements ESWordPutType
{
  ESWordPut(final ESDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static ESDatabaseQueryProviderType<
    String, DDatabaseUnit, ESWordPutType>
  provider()
  {
    return ESDatabaseQueryProvider.provide(
      ESWordPutType.class,
      ESWordPut::new
    );
  }

  @Override
  protected DDatabaseUnit onExecute(
    final ESDatabaseTransactionType transaction,
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
