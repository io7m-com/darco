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


package com.io7m.darco.postgres;

import com.io7m.darco.api.DDatabaseConfigurationType;
import com.io7m.darco.api.DUsernamePassword;

/**
 * The type of database configurations specific to PostgreSQL implementations.
 */

public interface DPQDatabaseConfigurationType
  extends DDatabaseConfigurationType
{
  /**
   * @return The database address
   */

  String databaseAddress();

  /**
   * @return The database port
   */

  int databasePort();

  /**
   * @return The database name
   */

  String databaseName();

  /**
   * @return Whether to connect to the database using TLS
   */

  boolean databaseUseTLS();

  /**
   * @return The database owner role
   */

  DUsernamePassword ownerRole();

  @Override
  default DUsernamePassword defaultRole()
  {
    return this.workerRole();
  }

  /**
   * @return The database worker role
   */

  DUsernamePassword workerRole();
}
