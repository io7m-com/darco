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

import com.io7m.darco.examples.postgresql.EPQDatabaseQueryProviderType;
import com.io7m.darco.examples.postgresql.EPQWordGet;
import com.io7m.darco.examples.postgresql.EPQWordPut;
import com.io7m.darco.examples.sqlite.ESDatabaseQueryProviderType;
import com.io7m.darco.examples.sqlite.ESWordGet;
import com.io7m.darco.examples.sqlite.ESWordPut;

/**
 * Minimalist, opinionated database access (Examples)
 */

module com.io7m.darco.examples
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.darco.api;
  requires com.io7m.darco.sqlite;
  requires com.io7m.darco.postgres;

  requires java.sql;

  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires io.opentelemetry.api;
  requires org.slf4j;
  requires org.xerial.sqlitejdbc;
  requires com.io7m.jxe.core;

  uses ESDatabaseQueryProviderType;
  uses EPQDatabaseQueryProviderType;

  provides ESDatabaseQueryProviderType
    with ESWordPut, ESWordGet;

  provides EPQDatabaseQueryProviderType
    with EPQWordPut, EPQWordGet;

  exports com.io7m.darco.examples.sqlite;
  exports com.io7m.darco.examples.postgresql;
}
