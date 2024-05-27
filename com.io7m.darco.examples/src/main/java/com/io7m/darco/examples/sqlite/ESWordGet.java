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
import java.util.Optional;

/**
 * The word-get query.
 */

public final class ESWordGet
  extends ESDatabaseQueryAbstract<DDatabaseUnit, Optional<String>>
  implements ESWordGetType
{
  ESWordGet(final ESDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static ESDatabaseQueryProviderType provider()
  {
    return ESDatabaseQueryProvider.provide(ESWordGetType.class, ESWordGet::new);
  }

  @Override
  protected Optional<String> onExecute(
    final ESDatabaseTransactionType transaction,
    final DDatabaseUnit text)
    throws SQLException
  {
    final var c = transaction.connection();

    try (var s = c.prepareStatement("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")) {
      try (var r = s.executeQuery()) {
        if (r.next()) {
          return Optional.of(r.getString(1));
        }
      }
    }

    return Optional.empty();
  }
}
