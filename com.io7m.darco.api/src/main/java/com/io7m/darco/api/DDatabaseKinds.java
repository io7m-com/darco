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

/**
 * Well-known database kinds.
 */

public final class DDatabaseKinds
{
  private static final RDottedName POSTGRESQL =
    new RDottedName("org.postgresql");

  private static final RDottedName SQLITE =
    new RDottedName("org.sqlite");

  private DDatabaseKinds()
  {

  }

  /**
   * The SQLite database.
   *
   * @return The kind name
   *
   * @see "https://www.sqlite.org"
   */

  public static RDottedName sqlite()
  {
    return SQLITE;
  }

  /**
   * The PostgreSQL database.
   *
   * @return The kind name
   *
   * @see "https://www.postgresql.org"
   */

  public static RDottedName postgreSQL()
  {
    return POSTGRESQL;
  }
}
