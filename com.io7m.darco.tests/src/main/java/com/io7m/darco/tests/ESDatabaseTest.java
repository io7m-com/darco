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


package com.io7m.darco.tests;

import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.examples.sqlite.ESDatabaseConfiguration;
import com.io7m.darco.examples.sqlite.ESDatabaseFactory;
import com.io7m.darco.examples.sqlite.ESDatabaseType;
import com.io7m.darco.examples.sqlite.ESWordGetType;
import com.io7m.darco.examples.sqlite.ESWordPutType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.io7m.darco.api.DDatabaseUnit.UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ESDatabaseTest
{
  private ESDatabaseFactory databases;
  private Path databaseFile;
  private ESDatabaseType database;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
    throws Exception
  {
    this.databaseFile =
      directory.resolve("database.db");
    this.databases =
      new ESDatabaseFactory();
    this.database =
      this.databases.open(
        new ESDatabaseConfiguration(
          Optional.empty(),
          DDatabaseTelemetryNoOp.get(),
          DDatabaseCreate.CREATE_DATABASE,
          DDatabaseUpgrade.UPGRADE_DATABASE,
          this.databaseFile
        ),
        event -> {

        }
      );
  }

  @Test
  public void testUsage()
    throws DDatabaseException
  {
    try (var c = this.database.openConnection()) {
      try (var t = c.openTransaction()) {
        final var qp = t.query(ESWordPutType.class);
        qp.execute("Word0");
        t.commit();
      }

      try (var t = c.openTransaction()) {
        final var qg = t.query(ESWordGetType.class);
        assertEquals("Word0", qg.execute(UNIT).orElseThrow());
      }
    }
  }

  @Test
  public void testCloseConnection()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      c.registerResource(() -> {
        closed.set(true);
      });
    }
    assertTrue(closed.get());
  }

  @Test
  public void testCloseTransaction()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      try (var t = c.openTransaction()) {
        t.registerResource(() -> {
          closed.set(true);
        });
      }
    }
    assertTrue(closed.get());
  }

  @Test
  public void testCloseTransactionImplicit()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      final var t = c.openTransaction();
      t.registerResource(() -> {
        closed.set(true);
      });
    }
    assertTrue(closed.get());
  }
}
